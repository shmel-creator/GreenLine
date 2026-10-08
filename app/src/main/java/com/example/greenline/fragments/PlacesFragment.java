package com.example.greenline.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.greenline.MainActivity;
import com.example.greenline.R;
import com.example.greenline.models.Place;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;

public class PlacesFragment extends Fragment {

    private static final String TAG = "PlacesFragment";
    private RecyclerView recyclerView;
    private PlacesAdapter adapter;
    private List<Place> placeList;
    private List<Place> allPlaces;
    private LinearLayout popularContainer;
    private SharedPreferences prefs;
    private ShimmerFrameLayout shimmer;

    // Для геолокации
    private double userLat = 58.0105;  // центр Перми (по умолчанию)
    private double userLng = 56.2342;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        return inflater.inflate(R.layout.fragment_places, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated");

        recyclerView = view.findViewById(R.id.recycler_places);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        shimmer = view.findViewById(R.id.shimmer);
        popularContainer = view.findViewById(R.id.popular_container);

        if (getActivity() != null) {
            prefs = getActivity().getSharedPreferences("GreenLinePrefs", Context.MODE_PRIVATE);
        }

        // Показываем анимацию загрузки
        showLoading();

        // Получаем местоположение пользователя
        getUserLocation();

        loadPlaces();

        adapter = new PlacesAdapter(placeList);
        recyclerView.setAdapter(adapter);

        // Скрываем анимацию после загрузки данных
        new Handler().postDelayed(() -> hideLoading(), 500);

        displayPopularPlaces();

        // Обработчики категорий
        LinearLayout catCulture = view.findViewById(R.id.cat_culture);
        LinearLayout catHistory = view.findViewById(R.id.cat_history);
        LinearLayout catArchitecture = view.findViewById(R.id.cat_architecture);
        LinearLayout catNature = view.findViewById(R.id.cat_nature);

        catCulture.setOnClickListener(v -> {
            List<Place> filtered = new ArrayList<>();
            for (Place p : allPlaces) {
                if (p.title.contains("Театр") || p.title.contains("Галерея")) {
                    filtered.add(p);
                }
            }
            updateList(filtered);
            Toast.makeText(getContext(), "Культурные достопримечательности", Toast.LENGTH_SHORT).show();
        });

        catHistory.setOnClickListener(v -> {
            List<Place> filtered = new ArrayList<>();
            for (Place p : allPlaces) {
                if (p.title.contains("Мешкова") || p.title.contains("Стела")) {
                    filtered.add(p);
                }
            }
            updateList(filtered);
            Toast.makeText(getContext(), "Исторические места", Toast.LENGTH_SHORT).show();
        });

        catArchitecture.setOnClickListener(v -> {
            List<Place> filtered = new ArrayList<>();
            for (Place p : allPlaces) {
                if (p.title.contains("уши") || p.title.contains("Мешкова")) {
                    filtered.add(p);
                }
            }
            updateList(filtered);
            Toast.makeText(getContext(), "Архитектурные объекты", Toast.LENGTH_SHORT).show();
        });

        catNature.setOnClickListener(v -> {
            List<Place> filtered = new ArrayList<>();
            for (Place p : allPlaces) {
                if (p.title.contains("Счастье")) {
                    filtered.add(p);
                }
            }
            updateList(filtered);
            Toast.makeText(getContext(), "Природные достопримечательности", Toast.LENGTH_SHORT).show();
        });
    }

    private void showLoading() {
        if (shimmer != null) {
            shimmer.setVisibility(View.VISIBLE);
            shimmer.startShimmer();
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void hideLoading() {
        if (shimmer != null) {
            shimmer.stopShimmer();
            shimmer.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    // Обновление списка при фильтрации
    private void updateList(List<Place> filteredPlaces) {
        adapter = new PlacesAdapter(filteredPlaces);
        recyclerView.setAdapter(adapter);
    }

    // Метод для вычисления расстояния между двумя точками (формула гаверсинусов)
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Радиус Земли в км
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // Получение местоположения пользователя
    private void getUserLocation() {
        if (getActivity() == null) return;

        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        Location lastLocation = null;

        try {
            lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation == null) {
                lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        if (lastLocation != null) {
            userLat = lastLocation.getLatitude();
            userLng = lastLocation.getLongitude();
            Log.d(TAG, "Location updated: " + userLat + ", " + userLng);
        } else {
            userLat = 58.0105;
            userLng = 56.2342;
            Toast.makeText(getContext(), "Не удалось определить местоположение, показано расстояние от центра Перми", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getUserLocation();
            if (adapter != null) adapter.notifyDataSetChanged();
        }
    }

    private void loadPlaces() {
        placeList = new ArrayList<>();
        allPlaces = new ArrayList<>();

        Place place1 = new Place("Дом Мешкова", "ул. Монастырская, 11",
                58.0149, 56.2507, 4.7, "Особняк XIX века", R.raw.dom_meshkova);
        Place place2 = new Place("Пермская художественная галерея", "Советская ул., 1, корп. 5",
                58.021205, 56.258369, 4.8, "Пермская деревянная скульптура", R.raw.galereya);
        Place place3 = new Place("Театр оперы", "ул. Петропавловская, 25а",
                58.015931, 56.245857, 4.9, "Символ Перми", R.raw.teatr);
        Place place4 = new Place("Стела трудовой доблести", "пл. Карла Маркса",
                57.997126, 56.262681, 4.6, "Вклад в Победу", R.raw.stela);
        Place place5 = new Place("Счастье не за горами", "Набережная Камы",
                58.0128, 56.2338, 4.8, "Красные буквы", R.raw.schastie);
        Place place6 = new Place("Пермяк соленые уши", "Комсомольский пр.",
                58.009745, 56.239738, 4.7, "Памятник солеварению", R.raw.ushi);

        placeList.add(place1);
        placeList.add(place2);
        placeList.add(place3);
        placeList.add(place4);
        placeList.add(place5);
        placeList.add(place6);

        allPlaces.add(place1);
        allPlaces.add(place2);
        allPlaces.add(place3);
        allPlaces.add(place4);
        allPlaces.add(place5);
        allPlaces.add(place6);
    }

    private void openRoute(double lat, double lng, String title, String address) {
        if (getActivity() == null) return;

        // Добавляем в историю
        Place tempPlace = new Place(title, address, lat, lng, 0);
        HistoryFragment.addToHistory(getContext(), tempPlace, "route");

        String uri = "yandexmaps://maps.yandex.ru/?rtext=~" + lat + "," + lng;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        if (intent.resolveActivity(getActivity().getPackageManager()) == null) {
            uri = "https://yandex.ru/maps/?rtext=~" + lat + "," + lng;
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        }
        startActivity(intent);
    }

    private void toggleFavorite(Place place) {
        if (prefs == null) return;
        String saved = prefs.getString("favorites", "");
        boolean isFavorite = saved.contains(place.title);
        SharedPreferences.Editor editor = prefs.edit();

        if (isFavorite) {
            String newFav = saved.replace(place.title, "").replace("||", "|");
            if (newFav.startsWith("|")) newFav = newFav.substring(1);
            if (newFav.endsWith("|")) newFav = newFav.substring(0, Math.max(0, newFav.length() - 1));
            editor.putString("favorites", newFav);
            Toast.makeText(getContext(), "Удалено из избранного", Toast.LENGTH_SHORT).show();
        } else {
            String newFav = saved.isEmpty() ? place.title : saved + "|" + place.title;
            editor.putString("favorites", newFav);
            editor.putString(place.title + "_address", place.address);
            editor.putFloat(place.title + "_lat", (float) place.lat);
            editor.putFloat(place.title + "_lng", (float) place.lng);
            editor.putFloat(place.title + "_rating", (float) place.rating);
            Toast.makeText(getContext(), "Добавлено в избранное", Toast.LENGTH_SHORT).show();
        }
        editor.apply();
    }

    private boolean isFavorite(String title) {
        if (prefs == null) return false;
        return prefs.getString("favorites", "").contains(title);
    }

    private void saveRating(String title, float rating) {
        if (prefs == null) return;
        prefs.edit().putFloat(title + "_user_rating", rating).apply();
        Log.d(TAG, "Сохранён рейтинг для " + title + ": " + rating);
    }

    private float loadRating(String title) {
        if (prefs == null) return 0;
        return prefs.getFloat(title + "_user_rating", 0);
    }

    private void displayPopularPlaces() {
        if (popularContainer == null) return;
        popularContainer.removeAllViews();
        for (int i = 0; i < 3 && i < allPlaces.size(); i++) {
            Place place = allPlaces.get(i);
            View itemView = createPopularItemView(place);
            popularContainer.addView(itemView);
        }
    }

    private View createPopularItemView(Place place) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_popular, null);

        TextView tvTitle = view.findViewById(R.id.tv_title);
        TextView tvAddress = view.findViewById(R.id.tv_address);
        TextView tvPrice = view.findViewById(R.id.tv_price);
        Button btnBook = view.findViewById(R.id.btn_book);

        tvTitle.setText(place.title);
        tvAddress.setText(place.address);
        tvPrice.setText("от 0");

        btnBook.setOnClickListener(v -> openRoute(place.lat, place.lng, place.title, place.address));

        return view;
    }

    private int getImageRes(String title) {
        switch (title) {
            case "Дом Мешкова": return R.drawable.dom_meshkova;
            case "Пермская художественная галерея": return R.drawable.galereya;
            case "Театр оперы": return R.drawable.teatr;
            case "Стела трудовой доблести": return R.drawable.stela;
            case "Счастье не за горами": return R.drawable.schastie;
            case "Пермяк соленые уши": return R.drawable.ushi;
            default: return 0;
        }
    }

    private void shareRoute(double lat, double lng, String title) {
        String url = "https://yandex.ru/maps/?rtext=~" + lat + "," + lng;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "📍 " + title + "\n\nПосмотри маршрут: " + url);
        startActivity(Intent.createChooser(shareIntent, "Поделиться маршрутом"));
    }

    class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.ViewHolder> {
        private List<Place> places;

        PlacesAdapter(List<Place> places) {
            this.places = places;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
            Place p = places.get(pos);
            holder.tvTitle.setText(p.title);
            holder.tvAddress.setText(p.address);

            // Реальное расстояние
            double distance = calculateDistance(userLat, userLng, p.lat, p.lng);
            String distanceText;
            if (distance < 1) {
                distanceText = String.format("~ %.0f м", distance * 1000);
            } else {
                distanceText = String.format("~ %.1f км", distance);
            }
            holder.tvDistance.setText(distanceText + " от вас");

            // Рейтинг от пользователей (RatingBar)
            float savedRating = loadRating(p.title);
            holder.ratingBar.setRating(savedRating);
            holder.ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                if (fromUser) {
                    saveRating(p.title, rating);
                    // Добавляем в историю посещений
                    HistoryFragment.addToHistory(getContext(), p, "rating");
                    Toast.makeText(getContext(), "Вы оценили " + p.title + " на " + rating + " звезд", Toast.LENGTH_SHORT).show();
                }
            });

            // Избранное
            if (isFavorite(p.title)) {
                holder.btnFavorite.setImageResource(R.drawable.ic_star_full);
                holder.btnFavorite.setColorFilter(0xFFFFC107);
            } else {
                holder.btnFavorite.setImageResource(R.drawable.ic_star_empty);
                holder.btnFavorite.setColorFilter(0xFF666666);
            }

            holder.btnRoute.setOnClickListener(v -> openRoute(p.lat, p.lng, p.title, p.address));

            holder.btnFavorite.setOnClickListener(v -> {
                toggleFavorite(p);
                if (isFavorite(p.title)) {
                    holder.btnFavorite.setImageResource(R.drawable.ic_star_full);
                    holder.btnFavorite.setColorFilter(0xFFFFC107);
                } else {
                    holder.btnFavorite.setImageResource(R.drawable.ic_star_empty);
                    holder.btnFavorite.setColorFilter(0xFF666666);
                }
            });

            holder.btnAudio.setOnClickListener(v -> {
                // Добавляем в историю посещений
                HistoryFragment.addToHistory(getContext(), p, "audio");
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).switchToAudioGuide();
                }
            });

            // Фото
            int imageRes = getImageRes(p.title);
            if (imageRes != 0) {
                holder.ivImage.setImageResource(imageRes);
            }

            holder.btnShare.setOnClickListener(v -> shareRoute(p.lat, p.lng, p.title));

            holder.btnReviews.setOnClickListener(v -> {
                ReviewsFragment reviewsFragment = ReviewsFragment.newInstance(p.title);
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.fragment_container, reviewsFragment)
                        .addToBackStack(null)
                        .commit();
            });
        }

        @Override
        public int getItemCount() {
            return places.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvAddress, tvDistance;
            Button btnRoute;
            ImageButton btnFavorite, btnAudio, btnShare;
            ImageView ivImage;
            RatingBar ratingBar;
            ImageButton btnReviews;

            ViewHolder(@NonNull View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tv_title);
                tvAddress = v.findViewById(R.id.tv_address);
                tvDistance = v.findViewById(R.id.tv_distance);
                btnRoute = v.findViewById(R.id.btn_route);
                btnFavorite = v.findViewById(R.id.btn_favorite);
                btnAudio = v.findViewById(R.id.btn_audio);
                btnShare = v.findViewById(R.id.btn_share);
                ivImage = v.findViewById(R.id.iv_image);
                ratingBar = v.findViewById(R.id.rating_bar);
                btnReviews = v.findViewById(R.id.btn_reviews);
            }
        }
    }

    @Override public void onStart() { super.onStart(); Log.d(TAG, "onStart"); }
    @Override public void onResume() { super.onResume(); Log.d(TAG, "onResume"); }
    @Override public void onPause() { super.onPause(); Log.d(TAG, "onPause"); }
    @Override public void onStop() { super.onStop(); Log.d(TAG, "onStop"); }
    @Override public void onDestroyView() { super.onDestroyView(); Log.d(TAG, "onDestroyView"); }
    @Override public void onDestroy() { super.onDestroy(); Log.d(TAG, "onDestroy"); }
    @Override public void onDetach() { super.onDetach(); Log.d(TAG, "onDetach"); }
}