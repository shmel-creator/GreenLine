package com.example.greenline.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.runtime.image.ImageProvider;

import com.example.greenline.R;
import com.example.greenline.models.Place;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment {

    private static final String TAG = "MapFragment";
    private static final int LOCATION_PERMISSION_CODE = 100;

    private RecyclerView recyclerView;
    private List<Place> placeList;
    private List<Place> allPlaces;
    private MapView mapView;
    private FusedLocationProviderClient fusedLocationClient;
    private Point myLocationPoint;
    private SearchView searchView;
    private MapObjectCollection placemarks;

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
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        recyclerView = view.findViewById(R.id.recycler_map);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        mapView = view.findViewById(R.id.mapview);
        if (mapView == null) {
            Log.e(TAG, "mapView is null!");
            return;
        }

        // Центрируем карту на Перми
        mapView.getMap().move(new CameraPosition(new Point(58.0105, 56.2342), 13, 0, 0));

        // Получаем коллекцию для меток
        placemarks = mapView.getMap().getMapObjects().addCollection();

        // Добавляем метки на карту
        addPlacemarks();

        // Загружаем список мест
        loadPlaces();
        allPlaces = new ArrayList<>(placeList);

        if (recyclerView != null) {
            recyclerView.setAdapter(new MapAdapter(placeList));
        }

        // Кнопка "Моё местоположение"
        Button btnMyLocation = view.findViewById(R.id.btn_my_location);
        if (btnMyLocation != null) {
            btnMyLocation.setOnClickListener(v -> showMyLocation());
        }

        // ★★★ ПОИСК ★★★
        searchView = view.findViewById(R.id.search_view);
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    searchPlaces(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (newText.isEmpty()) {
                        // Если поиск очищен, показываем все места
                        searchPlaces("");
                    } else {
                        searchPlaces(newText);
                    }
                    return true;
                }
            });
        }

        checkLocationPermission();
    }

    private void searchPlaces(String query) {
        List<Place> filteredPlaces = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            // Показываем все места
            filteredPlaces.addAll(allPlaces);
            showAllPlacemarks();
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (Place place : allPlaces) {
                if (place.title.toLowerCase().contains(lowerQuery) ||
                        place.address.toLowerCase().contains(lowerQuery)) {
                    filteredPlaces.add(place);
                }
            }
            // Показываем только найденные метки
            showFilteredPlacemarks(filteredPlaces);
        }

        // Обновляем список в RecyclerView
        if (recyclerView != null) {
            recyclerView.setAdapter(new MapAdapter(filteredPlaces));
        }

        // Если есть результат, центрируем карту на первом найденном месте
        if (!filteredPlaces.isEmpty()) {
            Place first = filteredPlaces.get(0);
            mapView.getMap().move(new CameraPosition(new Point(first.lat, first.lng), 15, 0, 0));
        } else if (query != null && !query.trim().isEmpty()) {
            Toast.makeText(getContext(), "Ничего не найдено", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAllPlacemarks() {
        placemarks.clear();
        for (int i = 0; i < placesPoints.length; i++) {
            placemarks.addPlacemark(
                    placesPoints[i],
                    ImageProvider.fromResource(requireContext(), R.drawable.ic_pin_green)
            ).setUserData(placeNames[i]);
        }
    }

    private void showFilteredPlacemarks(List<Place> filteredPlaces) {
        placemarks.clear();
        for (Place place : filteredPlaces) {
            // Находим координаты по названию
            for (int i = 0; i < placeNames.length; i++) {
                if (placeNames[i].equals(place.title)) {
                    placemarks.addPlacemark(
                            placesPoints[i],
                            ImageProvider.fromResource(requireContext(), R.drawable.ic_pin_green)
                    ).setUserData(place.title);
                    break;
                }
            }
        }
    }

    private void addPlacemarks() {
        showAllPlacemarks();
    }

    private void showMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                myLocationPoint = new Point(location.getLatitude(), location.getLongitude());
                mapView.getMap().move(new CameraPosition(myLocationPoint, 15, 0, 0));
                Toast.makeText(getContext(), "Вы здесь", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Не удалось определить местоположение", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buildRoute(double toLat, double toLng) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Нужно разрешение на геолокацию", Toast.LENGTH_LONG).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location == null) {
                Toast.makeText(getContext(), "Не удалось определить ваше местоположение", Toast.LENGTH_SHORT).show();
                return;
            }

            Point from = new Point(location.getLatitude(), location.getLongitude());
            Point to = new Point(toLat, toLng);

            String uri = "yandexmaps://maps.yandex.ru/?rtext=" + from.getLatitude() + "," + from.getLongitude()
                    + "~" + to.getLatitude() + "," + to.getLongitude();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            if (intent.resolveActivity(requireContext().getPackageManager()) == null) {
                uri = "https://yandex.ru/maps/?rtext=" + from.getLatitude() + "," + from.getLongitude()
                        + "~" + to.getLatitude() + "," + to.getLongitude();
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            }
            startActivity(intent);
        });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Разрешение получено", Toast.LENGTH_SHORT).show();
            showMyLocation();
        } else {
            Toast.makeText(getContext(), "Для геолокации нужно разрешение", Toast.LENGTH_LONG).show();
        }
    }

    private void loadPlaces() {
        placeList = new ArrayList<>();
        placeList.add(new Place("Дом Мешкова", "ул. Монастырская, 11", 58.0149, 56.2507, 4.7));
        placeList.add(new Place("Пермская художественная галерея", "Советская ул., 1, корп. 5", 58.021205, 56.258369, 4.8));
        placeList.add(new Place("Театр оперы", "ул. Петропавловская, 25а", 58.015931, 56.245857, 4.9));
        placeList.add(new Place("Стела трудовой доблести", "пл. Карла Маркса", 57.997126, 56.262681, 4.6));
        placeList.add(new Place("Счастье не за горами", "Набережная Камы", 58.0128, 56.2338, 4.8));
        placeList.add(new Place("Пермяк соленые уши", "Комсомольский пр.", 58.009745, 56.239738, 4.7));
    }

    private void openRoute(double lat, double lng) {
        if (getActivity() == null) return;
        String uri = "yandexmaps://maps.yandex.ru/?rtext=~" + lat + "," + lng;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        if (intent.resolveActivity(getActivity().getPackageManager()) == null) {
            uri = "https://yandex.ru/maps/?rtext=~" + lat + "," + lng;
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        }
        startActivity(intent);
    }

    class MapAdapter extends RecyclerView.Adapter<MapAdapter.ViewHolder> {
        private List<Place> places;
        MapAdapter(List<Place> places) { this.places = places; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_map_place, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
            Place p = places.get(pos);
            holder.tvTitle.setText(p.title);
            holder.tvAddress.setText(p.address);
            holder.tvRating.setText(p.rating + " ★");
            holder.btnRoute.setOnClickListener(v -> openRoute(p.lat, p.lng));
            holder.btnBuildRoute.setOnClickListener(v -> buildRoute(p.lat, p.lng));
        }

        @Override public int getItemCount() { return places.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvAddress, tvRating;
            Button btnRoute, btnBuildRoute;
            ViewHolder(@NonNull View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tv_title);
                tvAddress = v.findViewById(R.id.tv_address);
                tvRating = v.findViewById(R.id.tv_rating);
                btnRoute = v.findViewById(R.id.btn_route);
                btnBuildRoute = v.findViewById(R.id.btn_build_route);
            }
        }
    }

    @Override public void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        if (mapView != null) mapView.onStart();
        Log.d(TAG, "onStart");
    }

    @Override public void onStop() {
        if (mapView != null) mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
        Log.d(TAG, "onStop");
    }
}