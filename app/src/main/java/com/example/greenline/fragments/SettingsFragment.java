package com.example.greenline.fragments;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.example.greenline.MainActivity;
import com.example.greenline.R;
import com.example.greenline.services.WeatherNotificationScheduler;
import com.example.greenline.utils.ThemeHelper;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int NOTIFICATION_PERMISSION_CODE = 101;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // Запрашиваем разрешение на уведомления для Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }

        // Тестовое уведомление
        Preference testNotification = findPreference("pref_test_notification");
        if (testNotification != null) {
            testNotification.setOnPreferenceClickListener(preference -> {
                sendTestNotification();
                return true;
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        // Синхронизируем состояние планировщика
        SwitchPreferenceCompat switchNotifications = findPreference("notifications_enabled");
        if (switchNotifications != null) {
            boolean isEnabled = getPreferenceScreen().getSharedPreferences()
                    .getBoolean("notifications_enabled", true);
            if (isEnabled) {
                WeatherNotificationScheduler.scheduleDailyWeatherNotification(requireContext());
            } else {
                WeatherNotificationScheduler.cancelNotifications(requireContext());
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("dark_mode")) {
            boolean isDarkMode = sharedPreferences.getBoolean(key, false);
            ThemeHelper.setTheme(requireActivity(), isDarkMode);

            Intent intent = new Intent(requireActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            requireActivity().finish();
        }

        if (key.equals("notifications_enabled")) {
            boolean isEnabled = sharedPreferences.getBoolean(key, true);
            if (isEnabled) {
                WeatherNotificationScheduler.scheduleDailyWeatherNotification(requireContext());
            } else {
                WeatherNotificationScheduler.cancelNotifications(requireContext());
            }
        }
    }

    private void sendTestNotification() {
        NotificationManager manager = (NotificationManager)
                requireContext().getSystemService(android.content.Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
                return;
            }
        }

        // Создаём канал для Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "test_channel",
                    "Тестовые уведомления",
                    NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), "test_channel")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("🔔 Тестовое уведомление")
                .setContentText("Если вы видите это сообщение — уведомления работают корректно!")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        manager.notify(999, builder.build());
    }
}