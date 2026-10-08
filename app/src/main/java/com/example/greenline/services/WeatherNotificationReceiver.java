package com.example.greenline.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.greenline.MainActivity;
import com.example.greenline.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WeatherNotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "weather_channel";
    private static final String CHANNEL_NAME = "Прогноз погоды";
    private static final String PREFS_NAME = "weather_prefs";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Получаем сохранённую погоду (если есть)
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String lastWeather = prefs.getString("last_weather", "солнечно");
        String lastTemp = prefs.getString("last_temp", "+15");

        // Формируем сообщение
        String[] weatherMessages = {
                "🌤️ Доброе утро! Сегодня в Перми " + lastWeather + ", " + lastTemp + "°C",
                "🏛️ Не забудьте посетить достопримечательности Перми сегодня!",
                "🎧 Послушайте аудиогид по Зелёной линии Перми",
                "📍 Самые интересные места Перми ждут вас!"
        };

        String message = weatherMessages[(int) (Math.random() * weatherMessages.length)];

        showWeatherNotification(context, "Зелёная линия", message);
    }

    private void showWeatherNotification(Context context, String title, String body) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(channel);
        }

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}