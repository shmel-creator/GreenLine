package com.example.greenline.utils;

import android.app.Activity;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeHelper {

    public static final String PREF_NAME = "theme_prefs";
    public static final String KEY_DARK_MODE = "dark_mode";
    private static boolean isApplying = false;

    // Применение темы при запуске приложения
    public static void applyTheme(boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    // Загрузка сохранённой темы
    public static void loadTheme(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean(KEY_DARK_MODE, false);
        applyTheme(isDarkMode);
    }

    // Сохранение и применение темы
    public static void setTheme(Activity activity, boolean isDarkMode) {
        if (isApplying) return;
        isApplying = true;

        SharedPreferences prefs = activity.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_DARK_MODE, isDarkMode).apply();
        applyTheme(isDarkMode);

        isApplying = false;
    }

    // Получение текущего состояния темы
    public static boolean isDarkMode(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }
}