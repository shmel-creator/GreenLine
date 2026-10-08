package com.example.greenline;

import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.greenline.fragments.*;
import com.example.greenline.utils.SwipeDetector;
import com.example.greenline.utils.ThemeHelper;
import com.google.android.material.navigation.NavigationView;
import com.yandex.mapkit.MapKitFactory;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private GestureDetector gestureDetector;
    private int currentFragmentIndex = 0;

    private final Fragment[] fragments = {
            new PlacesFragment(),      // 0
            new MapFragment(),         // 1
            new MapSDKFragment(),      // 2
            new FavoritesFragment(),   // 3
            new NotesFragment(),       // 4
            new AudioGuideFragment(),  // 5
            new WeatherFragment(),     // 6
            new HistoryFragment(),     // 7
            new SettingsFragment()     // 8
    };

    private final int[] menuItemIds = {
            R.id.nav_places,
            R.id.nav_map,
            R.id.nav_map_sdk,
            R.id.nav_favorites,
            R.id.nav_notes,
            R.id.nav_audio,
            R.id.nav_weather,
            R.id.nav_history,
            R.id.nav_settings
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ★★★ 1. Сначала применяем тему ★★★
        ThemeHelper.loadTheme(this);

        // ★★★ 2. Стандартный onCreate ★★★
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");

        initViews();
        setupToolbar();
        setupSwipeDetection();
        setupNavigationView();

        if (savedInstanceState == null) {
            switchFragment(0);
        } else {
            currentFragmentIndex = savedInstanceState.getInt("currentFragmentIndex", 0);
            switchFragment(currentFragmentIndex);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentFragmentIndex", currentFragmentIndex);
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
            getSupportActionBar().setTitle("Зеленая линия");
        }
    }

    private void setupNavigationView() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_places) switchFragment(0);
            else if (id == R.id.nav_map) switchFragment(1);
            else if (id == R.id.nav_map_sdk) switchFragment(2);
            else if (id == R.id.nav_favorites) switchFragment(3);
            else if (id == R.id.nav_notes) switchFragment(4);
            else if (id == R.id.nav_audio) switchFragment(5);
            else if (id == R.id.nav_weather) switchFragment(6);
            else if (id == R.id.nav_history) switchFragment(7);
            else if (id == R.id.nav_settings) switchFragment(8);
            else if (id == R.id.nav_about) {
                new AlertDialog.Builder(this)
                        .setTitle("О приложении")
                        .setMessage("Зеленая линия - гид по достопримечательностям Перми\n\nВерсия 1.0\n\n• Карта с маршрутами\n• Аудиогид\n• Избранное\n• Заметки\n• История посещений\n• Тёмная тема\n• Прогноз погоды")
                        .setPositiveButton("OK", null)
                        .show();
            } else if (id == R.id.nav_exit) {
                finish();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void switchFragment(int index) {
        currentFragmentIndex = index;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.fragment_container, fragments[index]);
        transaction.commit();

        if (index < menuItemIds.length) {
            navigationView.setCheckedItem(menuItemIds[index]);
        }

        String title = getFragmentTitle(index);
        if (getSupportActionBar() != null && title != null) {
            getSupportActionBar().setTitle(title);
        }

        Log.d(TAG, "switchFragment -> " + index);
    }

    private String getFragmentTitle(int index) {
        switch (index) {
            case 0: return "Места";
            case 1: return "Карта";
            case 2: return "Карта (SDK)";
            case 3: return "Избранное";
            case 4: return "Заметки";
            case 5: return "Аудиогид";
            case 6: return "Погода";
            case 7: return "История";
            case 8: return "Настройки";
            default: return "Зеленая линия";
        }
    }

    private void setupSwipeDetection() {
        SwipeDetector.SwipeListener listener = new SwipeDetector.SwipeListener() {
            @Override
            public void onSwipeLeft() {
                if (currentFragmentIndex < fragments.length - 1) {
                    switchFragment(currentFragmentIndex + 1);
                }
            }
            @Override
            public void onSwipeRight() {
                if (currentFragmentIndex > 0) {
                    switchFragment(currentFragmentIndex - 1);
                }
            }
        };
        gestureDetector = new GestureDetector(this, new SwipeDetector(listener));

        findViewById(R.id.fragment_container).setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return false;
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void switchToAudioGuide() {
        switchFragment(5);
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            MapKitFactory.getInstance().onStart();
        } catch (Exception e) {
            Log.e(TAG, "MapKit onStart error: " + e.getMessage());
        }
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onStop() {
        try {
            MapKitFactory.getInstance().onStop();
        } catch (Exception e) {
            Log.e(TAG, "MapKit onStop error: " + e.getMessage());
        }
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override protected void onResume() { super.onResume(); Log.d(TAG, "onResume"); }
    @Override protected void onPause() { super.onPause(); Log.d(TAG, "onPause"); }
    @Override protected void onDestroy() { super.onDestroy(); Log.d(TAG, "onDestroy"); }
}