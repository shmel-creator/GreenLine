package com.example.greenline.utils;

import android.content.Context;
import android.util.Log;
import com.yandex.mapkit.MapKitFactory;

public class MapKitInitializer {
    private static final String TAG = "MapKitInitializer";
    private static boolean isInitialized = false;
    private static final String API_KEY = "787f5896-fef3-488d-a5ad-b2ef5ad3b03c";

    public static void initialize(Context context) {
        if (!isInitialized) {
            Log.d(TAG, "Первый вызов initialize()");
            // setApiKey() должен быть ПЕРВЫМ вызовом MapKitFactory
            MapKitFactory.setApiKey(API_KEY);
            MapKitFactory.initialize(context);
            isInitialized = true;
        } else {
            Log.d(TAG, "MapKit уже инициализирован, пропускаем");
        }
    }

    public static boolean isInitialized() {
        return isInitialized;
    }
}