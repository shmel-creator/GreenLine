package com.example.greenline.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.greenline.R;
import com.example.greenline.models.Place;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    private static final String TAG = "FavoritesFragment";
    private RecyclerView recyclerView;
    private List<Place> favorites;
    private SharedPreferences prefs;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_favorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (getActivity() != null) {
            prefs = getActivity().getSharedPreferences("GreenLinePrefs", Context.MODE_PRIVATE);
        }

        loadFavorites();
        recyclerView.setAdapter(new FavoritesAdapter(favorites));
    }

    private void loadFavorites() {
        favorites = new ArrayList<>();
        if (prefs == null) return;

        String saved = prefs.getString("favorites", "");
        if (saved.isEmpty()) return;

        String[] titles = saved.split("\\|");
        for (String title : titles) {
            String address = prefs.getString(title + "_address", "");
            float lat = prefs.getFloat(title + "_lat", 0);
            float lng = prefs.getFloat(title + "_lng", 0);
            float rating = prefs.getFloat(title + "_rating", 0);
            if (!address.isEmpty()) {
                favorites.add(new Place(title, address, lat, lng, rating));
            }
        }
    }

    private void saveFavorites() {
        if (prefs == null) return;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < favorites.size(); i++) {
            if (i > 0) sb.append("|");
            sb.append(favorites.get(i).title);
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("favorites", sb.toString());
        for (Place p : favorites) {
            editor.putString(p.title + "_address", p.address);
            editor.putFloat(p.title + "_lat", (float) p.lat);
            editor.putFloat(p.title + "_lng", (float) p.lng);
            editor.putFloat(p.title + "_rating", (float) p.rating);
        }
        editor.apply();
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

    private void removeFromFavorites(int position) {
        new AlertDialog.Builder(getContext())
                .setTitle("Удалить")
                .setMessage("Удалить " + favorites.get(position).title + " из избранного?")
                .setPositiveButton("Да", (dialog, which) -> {
                    favorites.remove(position);
                    saveFavorites();
                    recyclerView.getAdapter().notifyItemRemoved(position);
                    Toast.makeText(getContext(), "Удалено", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Нет", null)
                .show();
    }

    class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {
        private List<Place> places;
        FavoritesAdapter(List<Place> places) { this.places = places; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
            Place p = places.get(pos);
            holder.tvTitle.setText(p.title);
            holder.tvAddress.setText(p.address);
            holder.tvRating.setText(p.rating + " ★");
            holder.btnRoute.setOnClickListener(v -> openRoute(p.lat, p.lng));
            holder.btnRemove.setOnClickListener(v -> removeFromFavorites(pos));
        }

        @Override public int getItemCount() { return places.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvAddress, tvRating;
            Button btnRoute, btnRemove;
            ViewHolder(@NonNull View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tv_title);
                tvAddress = v.findViewById(R.id.tv_address);
                tvRating = v.findViewById(R.id.tv_rating);
                btnRoute = v.findViewById(R.id.btn_route);
                btnRemove = v.findViewById(R.id.btn_remove);
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