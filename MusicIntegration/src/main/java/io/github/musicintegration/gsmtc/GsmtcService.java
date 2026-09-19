/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package io.github.musicintegration.gsmtc;

import io.github.musicintegration.gsmtc.MediaState;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GsmtcService {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"musicintegration");
    private static final AtomicReference<MediaState> LATEST = new AtomicReference<MediaState>(MediaState.EMPTY);
    private static final AtomicReference<String> THUMB_B64 = new AtomicReference<String>("");
    private static volatile String lastThumbTitle = "";
    private static volatile boolean thumbFetching = false;
    private static final Object IO_LOCK = new Object();
    private static final Object CMD_LOCK = new Object();
    private static final Object THUMB_LOCK = new Object();
    private static volatile Process process;
    private static volatile BufferedReader reader;
    private static volatile BufferedWriter writer;
    private static volatile Process cmdProcess;
    private static volatile BufferedReader cmdReader;
    private static volatile BufferedWriter cmdWriter;
    private static volatile Process thumbProcess;
    private static volatile BufferedReader thumbReader;
    private static volatile BufferedWriter thumbWriter;
    private static volatile ScheduledExecutorService scheduler;
    private static volatile Path scriptPath;
    private static volatile String pythonExe;

    private GsmtcService() {
    }

    public static MediaState getLatest() {
        MediaState s = LATEST.get();
        String thumb = THUMB_B64.get();
        if (!thumb.equals(s.thumbBase64)) {
            return new MediaState(s.title, s.artist, s.appUserModelId, s.positionMs, s.durationMs, s.playbackState, thumb, s.peak, s.sampledAtEpochMs);
        }
        return s;
    }

    public static void start() {
        if (scheduler != null) {
            return;
        }
        pythonExe = GsmtcService.findPython();
        if (pythonExe == null) {
            LOGGER.warn("[MusicIntegration] Python not found \u2014 media info unavailable.");
            return;
        }
        try {
            scriptPath = GsmtcService.extractScript();
        }
        catch (Exception e) {
            LOGGER.error("[MusicIntegration] Failed to extract Python bridge", (Throwable)e);
            return;
        }
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "musicintegration-gsmtc");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(() -> {
            try {
                String line = GsmtcService.writeCommandReadResponse("POLL");
                MediaState next = MediaState.parse(line);
                LATEST.set(next);
                String trackKey = next.appUserModelId + "|" + next.title;
                if (!trackKey.equals(lastThumbTitle)) {
                    lastThumbTitle = trackKey;
                    THUMB_B64.set("");
                    thumbFetching = false;
                    LOGGER.info("[MusicIntegration] track changed, fetching thumb for: {}", (Object)trackKey);
                    if (!thumbFetching) {
                        thumbFetching = true;
                        Thread thumbThread = new Thread(() -> {
                            try {
                                String b64;
                                Object object = THUMB_LOCK;
                                synchronized (object) {
                                    GsmtcService.ensureThumbProcess();
                                    thumbWriter.write("THUMB\n");
                                    thumbWriter.flush();
                                    long deadline = System.currentTimeMillis() + 15000L;
                                    while (!thumbReader.ready() && System.currentTimeMillis() < deadline) {
                                        Thread.sleep(50L);
                                    }
                                    b64 = thumbReader.ready() ? thumbReader.readLine() : null;
                                }
                                LOGGER.info("[MusicIntegration] thumb resp len={}", b64 == null ? "null" : Integer.valueOf(b64.length()));
                                if (b64 == null) {
                                    LOGGER.warn("[MusicIntegration] thumb process exited, alive={}", (Object)(thumbProcess != null && thumbProcess.isAlive() ? 1 : 0));
                                }
                                boolean valid = b64 != null && b64.length() > 100 && !b64.startsWith("{");
                                THUMB_B64.set(valid ? b64 : "");
                            }
                            catch (Exception e) {
                                LOGGER.warn("[MusicIntegration] thumb FAILED: {}", (Object)e.toString());
                                Object object = THUMB_LOCK;
                                synchronized (object) {
                                    GsmtcService.closeThumbProcess();
                                }
                            }
                            finally {
                                thumbFetching = false;
                            }
                        }, "musicintegration-thumb");
                        thumbThread.setDaemon(true);
                        thumbThread.start();
                    }
                }
            }
            catch (Exception e) {
                LOGGER.debug("GSMTC poll failed: {}", (Object)e.toString());
            }
        }, 0L, 100L, TimeUnit.MILLISECONDS);
        LOGGER.info("[MusicIntegration] Started with Python bridge: {}", (Object)pythonExe);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void stop() {
        ScheduledExecutorService s = scheduler;
        scheduler = null;
        if (s != null) {
            s.shutdownNow();
        }
        Object object = IO_LOCK;
        synchronized (object) {
            GsmtcService.closeProcess();
        }
        object = CMD_LOCK;
        synchronized (object) {
            GsmtcService.closeCmdProcess();
        }
        object = THUMB_LOCK;
        synchronized (object) {
            GsmtcService.closeThumbProcess();
        }
    }

    public static void skipNext() {
        GsmtcService.sendControlCommand("SKIP_NEXT");
    }

    public static void skipPrevious() {
        GsmtcService.sendControlCommand("SKIP_PREV");
    }

    public static void togglePlayPause() {
        GsmtcService.sendControlCommand("PLAY_PAUSE");
    }

    public static void seek(long posMs) {
        GsmtcService.sendControlCommand("SEEK " + posMs);
    }

    private static void sendControlCommand(String cmd) {
        Thread t = new Thread(() -> {
            try {
                Object object = CMD_LOCK;
                synchronized (object) {
                    GsmtcService.ensureCmdProcess();
                    cmdWriter.write(cmd + "\n");
                    cmdWriter.flush();
                    String resp = cmdReader.readLine();
                    LOGGER.info("[MusicIntegration] cmd={} resp={}", (Object)cmd, (Object)resp);
                }
            }
            catch (Exception e) {
                LOGGER.warn("[MusicIntegration] cmd FAILED '{}': {}", (Object)cmd, (Object)e.toString());
                Object object = CMD_LOCK;
                synchronized (object) {
                    GsmtcService.closeCmdProcess();
                }
            }
        }, "musicintegration-cmd");
        t.setDaemon(true);
        t.start();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static String writeCommandReadResponse(String command) throws Exception {
        Object object = IO_LOCK;
        synchronized (object) {
            GsmtcService.ensureProcess();
            writer.write(command + "\n");
            writer.flush();
            String line = reader.readLine();
            return line != null ? line : "{}";
        }
    }

    private static void ensureProcess() throws Exception {
        if (process != null && process.isAlive()) {
            return;
        }
        GsmtcService.closeProcess();
        ProcessBuilder pb = new ProcessBuilder(pythonExe, "-u", scriptPath.toAbsolutePath().toString());
        pb.redirectErrorStream(false);
        Process p = process = pb.start();
        Thread errThread = new Thread(() -> {
            try (BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream(), StandardCharsets.UTF_8));){
                String l;
                while ((l = err.readLine()) != null) {
                    LOGGER.warn("[gsmtc stderr] {}", (Object)l);
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }, "musicintegration-stderr");
        errThread.setDaemon(true);
        errThread.start();
        reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
        writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
    }

    private static void closeProcess() {
        try {
            if (writer != null) {
                writer.close();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            if (reader != null) {
                reader.close();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (process != null) {
            process.destroyForcibly();
        }
        writer = null;
        reader = null;
        process = null;
    }

    private static void ensureCmdProcess() throws Exception {
        if (cmdProcess != null && cmdProcess.isAlive()) {
            return;
        }
        GsmtcService.closeCmdProcess();
        ProcessBuilder pb = new ProcessBuilder(pythonExe, "-u", scriptPath.toAbsolutePath().toString());
        pb.redirectErrorStream(false);
        Process p = cmdProcess = pb.start();
        Thread err = new Thread(() -> {
            try {
                p.getErrorStream().transferTo(OutputStream.nullOutputStream());
            }
            catch (Exception exception) {
                // empty catch block
            }
        }, "musicintegration-cmd-err");
        err.setDaemon(true);
        err.start();
        cmdReader = new BufferedReader(new InputStreamReader(cmdProcess.getInputStream(), StandardCharsets.UTF_8));
        cmdWriter = new BufferedWriter(new OutputStreamWriter(cmdProcess.getOutputStream(), StandardCharsets.UTF_8));
    }

    private static void closeCmdProcess() {
        try {
            if (cmdWriter != null) {
                cmdWriter.close();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            if (cmdReader != null) {
                cmdReader.close();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (cmdProcess != null) {
            cmdProcess.destroyForcibly();
        }
        cmdWriter = null;
        cmdReader = null;
        cmdProcess = null;
    }

    private static void ensureThumbProcess() throws Exception {
        if (thumbProcess != null && thumbProcess.isAlive()) {
            return;
        }
        GsmtcService.closeThumbProcess();
        ProcessBuilder pb = new ProcessBuilder(pythonExe, "-u", scriptPath.toAbsolutePath().toString());
        pb.redirectErrorStream(false);
        pb.redirectInput(ProcessBuilder.Redirect.PIPE);
        Process p = thumbProcess = pb.start();
        Thread err = new Thread(() -> {
            try (BufferedReader er = new BufferedReader(new InputStreamReader(p.getErrorStream(), StandardCharsets.UTF_8));){
                String l;
                while ((l = er.readLine()) != null) {
                    LOGGER.warn("[gsmtc-thumb stderr] {}", (Object)l);
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }, "musicintegration-thumb-err");
        err.setDaemon(true);
        err.start();
        thumbReader = new BufferedReader(new InputStreamReader(thumbProcess.getInputStream(), StandardCharsets.UTF_8));
        thumbWriter = new BufferedWriter(new OutputStreamWriter(thumbProcess.getOutputStream(), StandardCharsets.UTF_8));
    }

    private static void closeThumbProcess() {
        try {
            if (thumbWriter != null) {
                thumbWriter.close();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            if (thumbReader != null) {
                thumbReader.close();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (thumbProcess != null) {
            thumbProcess.destroyForcibly();
        }
        thumbWriter = null;
        thumbReader = null;
        thumbProcess = null;
    }

    private static Path extractScript() throws Exception {
        Path dir = FabricLoader.getInstance().getConfigDir().resolve("musicintegration");
        Files.createDirectories(dir, new FileAttribute[0]);
        Path out = dir.resolve("gsmtc_bridge.py");
        try (InputStream in = Objects.requireNonNull(GsmtcService.class.getResourceAsStream("/musicintegration/scripts/gsmtc_bridge.py"), "gsmtc_bridge.py missing from jar");){
            Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
        }
        return out;
    }

    private static String findPython() {
        File[] dirs;
        File pyBase;
        for (String candidate : new String[]{"python", "python3", "py"}) {
            try {
                Process p = new ProcessBuilder(candidate, "--version").start();
                if (!p.waitFor(3L, TimeUnit.SECONDS) || p.exitValue() != 0 || !GsmtcService.checkWinsdk(candidate)) continue;
                return candidate;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData != null && (pyBase = new File(localAppData, "Programs\\Python")).isDirectory() && (dirs = pyBase.listFiles(File::isDirectory)) != null) {
            for (File dir : dirs) {
                File exe = new File(dir, "python.exe");
                if (!exe.exists() || !GsmtcService.checkWinsdk(exe.getAbsolutePath())) continue;
                return exe.getAbsolutePath();
            }
        }
        return null;
    }

    private static boolean checkWinsdk(String exe) {
        try {
            new ProcessBuilder(exe, "-m", "pip", "install", "pycaw", "--quiet").start().waitFor(30L, TimeUnit.SECONDS);
            Process p = new ProcessBuilder(exe, "-c", "import winsdk.windows.media.control; import pycaw").start();
            return p.waitFor(5L, TimeUnit.SECONDS) && p.exitValue() == 0;
        }
        catch (Exception e) {
            return false;
        }
    }
}

