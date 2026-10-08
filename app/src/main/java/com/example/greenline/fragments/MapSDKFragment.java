package com.example.greenline.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.greenline.R;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.runtime.image.ImageProvider;

public class MapSDKFragment extends Fragment {

    private static final String TAG = "MapSDKFragment";
    private MapView mapView;

    // Координаты достопримечательностей Перми
    private final Point[] placesPoints = {
            new Point(58.0149, 56.2507),      // Дом Мешкова
            new Point(58.021205, 56.258369),  // Художественная галерея
            new Point(58.015931, 56.245857),  // Театр оперы
            new Point(57.997126, 56.262681),  // Стела
            new Point(58.0128, 56.2338),      // Счастье не за горами
            new Point(58.009745, 56.239738)   // Пермяк соленые уши
    };

    private final String[] placeNames = {
            "Дом Мешкова",
            "Художественная галерея",
            "Театр оперы",
            "Стела трудовой доблести",
            "Счастье не за горами",
            "Пермяк соленые уши"
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map_sdk, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = view.findViewById(R.id.mapview);
        if (mapView == null) {
            Log.e(TAG, "mapView is null!");
            Toast.makeText(getContext(), "Ошибка: mapView не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        // Центр Перми
        mapView.getMap().move(new CameraPosition(new Point(58.0105, 56.2342), 13, 0, 0));

        // Добавляем метки
        for (int i = 0; i < placesPoints.length; i++) {
            mapView.getMap().getMapObjects().addPlacemark(
                    placesPoints[i],
                    ImageProvider.fromResource(requireContext(), R.drawable.ic_pin_green)
            ).setUserData(placeNames[i]);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        if (mapView != null) mapView.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    public void onStop() {
        if (mapView != null) mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
        Log.d(TAG, "onStop");
    }
}