package com.example.greenline.fragments;

import android.content.Context;
import android.content.SharedPreferences;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.greenline.R;
import com.example.greenline.models.Place;
import com.example.greenline.models.VisitHistory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryFragment extends Fragment {

    private static final String TAG = "HistoryFragment";
    private static final String HISTORY_PREFS = "visit_history";
    private static final String HISTORY_KEY = "history_list";

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private List<VisitHistory> historyList;
    private SharedPreferences prefs;
    private TextView tvEmpty;
    private Button btnClearHistory;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_history);
        tvEmpty = view.findViewById(R.id.tv_empty);
        btnClearHistory = view.findViewById(R.id.btn_clear_history);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (getActivity() != null) {
            prefs = getActivity().getSharedPreferences(HISTORY_PREFS, Context.MODE_PRIVATE);
        }

        loadHistory();
        adapter = new HistoryAdapter(historyList);
        recyclerView.setAdapter(adapter);

        // Кнопка очистки истории
        if (btnClearHistory != null) {
            btnClearHistory.setOnClickListener(v -> clearHistory());
            btnClearHistory.setVisibility(historyList.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    private void loadHistory() {
        historyList = new ArrayList<>();
        if (prefs == null) return;

        String json = prefs.getString(HISTORY_KEY, "");
        if (!json.isEmpty()) {
            try {
                Gson gson = new Gson();
                // Используем правильный TypeToken из Gson
                Type type = new TypeToken<List<VisitHistory>>() {}.getType();
                historyList = gson.fromJson(json, type);
                // Показываем от новых к старым (обратный порядок)
                if (historyList != null) {
                    Collections.reverse(historyList);
                } else {
                    historyList = new ArrayList<>();
                }
            } catch (Exception e) {
                Log.e(TAG, "Ошибка загрузки истории: " + e.getMessage());
                historyList = new ArrayList<>();
            }
        }

        updateEmptyView();
    }

    // Публичный метод для добавления в историю (вызывается из PlacesFragment)
    public static void addToHistory(Context context, Place place, String actionType) {
        if (context == null || place == null) return;

        SharedPreferences prefs = context.getSharedPreferences(HISTORY_PREFS, Context.MODE_PRIVATE);
        Gson gson = new Gson();

        String json = prefs.getString(HISTORY_KEY, "");
        List<VisitHistory> historyList = new ArrayList<>();

        if (!json.isEmpty()) {
            try {
                Type type = new TypeToken<List<VisitHistory>>() {}.getType();
                historyList = gson.fromJson(json, type);
                if (historyList == null) historyList = new ArrayList<>();
            } catch (Exception e) {
                Log.e("History", "Ошибка парсинга: " + e.getMessage());
                historyList = new ArrayList<>();
            }
        }

        // Добавляем новую запись
        VisitHistory visit = new VisitHistory(
                place.title,
                place.address,
                place.lat,
                place.lng,
                actionType
        );
        historyList.add(visit);

        // Ограничим историю 50 записями
        if (historyList.size() > 50) {
            historyList = historyList.subList(historyList.size() - 50, historyList.size());
        }

        // Сохраняем
        String newJson = gson.toJson(historyList);
        prefs.edit().putString(HISTORY_KEY, newJson).apply();

        Log.d("History", "Добавлено: " + place.title + " - " + actionType);
    }

    // Очистка истории
    private void clearHistory() {
        if (prefs != null) {
            prefs.edit().remove(HISTORY_KEY).apply();
            historyList.clear();
            adapter.notifyDataSetChanged();
            updateEmptyView();
            if (btnClearHistory != null) {
                btnClearHistory.setVisibility(View.GONE);
            }
            Toast.makeText(getContext(), "История очищена", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateEmptyView() {
        if (tvEmpty != null) {
            boolean isEmpty = historyList == null || historyList.isEmpty();
            tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            if (recyclerView != null) {
                recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            }
            if (btnClearHistory != null) {
                btnClearHistory.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            }
        }
    }

    class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private List<VisitHistory> history;

        HistoryAdapter(List<VisitHistory> history) {
            this.history = history != null ? history : new ArrayList<>();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_history, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
            if (pos >= history.size()) return;
            VisitHistory v = history.get(pos);
            holder.tvIcon.setText(v.getActionIcon());
            holder.tvTitle.setText(v.placeTitle);
            holder.tvAddress.setText(v.placeAddress);
            holder.tvDate.setText(v.getFormattedDate());

            holder.itemView.setOnClickListener(view -> {
                Toast.makeText(getContext(), v.placeTitle + " - " + v.getFormattedDate(), Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return history.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvIcon, tvTitle, tvAddress, tvDate;
            ViewHolder(@NonNull View v) {
                super(v);
                tvIcon = v.findViewById(R.id.tv_icon);
                tvTitle = v.findViewById(R.id.tv_title);
                tvAddress = v.findViewById(R.id.tv_address);
                tvDate = v.findViewById(R.id.tv_date);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistory();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override public void onStart() { super.onStart(); Log.d(TAG, "onStart"); }
    @Override public void onStop() { super.onStop(); Log.d(TAG, "onStop"); }
}