package com.shampoon.speedysclient.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Тексты ChangeLog. Новые блоки добавляйте в начало списка {@link #BLOCKS}.
 */
public final class SpeedysChangelogData {
    private SpeedysChangelogData() {
    }

    public static final List<String[]> BLOCKS = Collections.unmodifiableList(Arrays.asList(
            new String[]{
                    "----------------",
                    "-- 24.05.26 --",
                    "Обновление 1.8.0(Релиз)",
                    "Уведомления",
                    "Что нового?",
                    "◉В обновлении добавили",
                    "уведомления для функций,",
                    "и добавили меню с ЧенджЛогами",
                    "",
                    "-Добавленно",
                    "1.Новое меню с ChangeLogs",
                    "2.Микс градиентов для некоторых функций:",
                    "Trail, Block Overlay, China Hat,",
                    "Nimb",
                    "",
                    "-Измененно",
                    "1.Уведомления от функций:",
                    "Item Swap, Trap Timer",
                    "----------------",
            },
            new String[]{
                    "----------------",
                    "-- 18.05.26 --",
                    "Обновление 1.7.1(свеж.)",
                    "Маленькие изменения",
                    "Что нового?",
                    "◉В обновлении добавили",
                    "Множество новых",
                    "изменений и функций",
                    "",
                    "-Добавленно",
                    "1.WaterMark Toggle",
                    "2.Eat Helper(возвр.)",
                    "3.Potion Highliter",
                    "4.Добавили новую",
                    "настройку в Custom",
                    "World",
                    "",
                    "-Измененно",
                    "1.Чуть-чуть изменили",
                    "Item Swap",
                    "----------------",
            }));

}
