package com.example.greenline;

import android.app.Application;
import android.util.Log;

import com.example.greenline.services.WeatherNotificationScheduler;
import com.yandex.mapkit.MapKitFactory;

public class GreenLineApplication extends Application {

    private static final String TAG = "GreenLineApplication";
    private static final String API_KEY = "787f5896-fef3-488d-a5ad-b2ef5ad3b03c";
    private static boolean isInitialized = false;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "=== ПРИЛОЖЕНИЕ ЗАПУЩЕНО (самый первый вызов) ===");

        // ★★★★★ ВАЖНО! setApiKey() ДОЛЖЕН БЫТЬ ПЕРВЫМ ★★★★★
        MapKitFactory.setApiKey(API_KEY);

        // Инициализация Яндекс Карт
        MapKitFactory.initialize(this);
        isInitialized = true;

        Log.d(TAG, "Яндекс Карты инициализированы!");

        // Добавьте в конец метода onCreate()
        if (getSharedPreferences("fcm_prefs", MODE_PRIVATE).getBoolean("notifications_enabled", true)) {
            WeatherNotificationScheduler.scheduleDailyWeatherNotification(this);
        }
    }

    public static boolean isInitialized() {
        return isInitialized;
    }
}