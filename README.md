# SpeedysClient

![Minecraft](https://www.minecraft.net/)
![Fabric Loader]((https://fabricmc.net/use/installer/))
![Java](https://www.java.com/ru/download/)
![License](https://github.com/Korchagin060912X/SpeedyClient/blob/master/SpeedysClient/LICENSE)

Fabric мод для Minecraft **1.21.8**.

---

## Сборка JAR

### Требования

- **Java 21** (JDK, не JRE) — [скачать](https://adoptium.net/)
- **Git** — для клонирования репозиториев

> Gradle скачивается автоматически через `gradlew`, ставить отдельно не нужно.

---

### Структура папок

Этот репозиторий подтягивает исходники из **соседних папок**. Убедись, что у тебя рядом лежат все нужные репозитории:

```
папка/
├── SpeedysClient/        ← этот репозиторий
├── PVPUtils/
│   └── PVPUtils-new/
├── AttackIndicator/
├── CoolDownItems/
├── DurabilityArmorTint/
├── PearlLandingPredictor/
└── MusicIntegration/
```

Если каких-то папок нет — сборка упадёт с ошибкой.

---

### Сборка

Открой терминал в папке `SpeedysClient` и запусти:

**Windows:**
```bat
gradlew.bat build
```

**Linux / macOS:**
```bash
./gradlew build
```

Готовый JAR появится в:
```
build/libs/speedysclient-<версия>.jar
```

Используй файл **без** суффикса `-dev` или `-sources`.

---

### Установить мод прямо в `.minecraft/mods`

```bat
gradlew.bat installLocalMod
```

Команда сама скопирует JAR в `%APPDATA%\.minecraft\mods` (Windows) или `~/.minecraft/mods`.

Для кастомного пути:
```bat
gradlew.bat installLocalMod -PspeedysModsDir=C:/Games/MC/mods
```

---

### Если что-то пошло не так

- Убедись что Java 21 стоит и прописана в `PATH` — проверь: `java -version`
- Убедись что все соседние репозитории клонированы и лежат в правильных папках
- Попробуй очистить кэш: `gradlew.bat clean` и потом снова `build`
