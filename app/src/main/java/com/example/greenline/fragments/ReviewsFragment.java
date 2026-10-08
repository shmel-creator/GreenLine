package com.example.greenline.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.greenline.R;
import com.example.greenline.models.Review;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReviewsFragment extends Fragment {

    private static final String PREFS_NAME = "reviews_prefs";
    private static final String REVIEWS_KEY = "reviews_list";

    private RecyclerView recyclerView;
    private ReviewsAdapter adapter;
    private List<Review> reviewsList;
    private SharedPreferences prefs;
    private String placeTitle;
    private TextView tvAverageRating, tvReviewsCount;
    private Button btnAddReview;

    public static ReviewsFragment newInstance(String placeTitle) {
        ReviewsFragment fragment = new ReviewsFragment();
        Bundle args = new Bundle();
        args.putString("place_title", placeTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            placeTitle = getArguments().getString("place_title");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reviews, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_reviews);
        tvAverageRating = view.findViewById(R.id.tv_average_rating);
        tvReviewsCount = view.findViewById(R.id.tv_reviews_count);
        btnAddReview = view.findViewById(R.id.btn_add_review);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (getActivity() != null) {
            prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }

        loadReviews();
        updateStats();

        adapter = new ReviewsAdapter(reviewsList);
        recyclerView.setAdapter(adapter);

        btnAddReview.setOnClickListener(v -> showAddReviewDialog());
    }

    private void loadReviews() {
        reviewsList = new ArrayList<>();
        if (prefs == null) return;

        String json = prefs.getString(REVIEWS_KEY, "");
        if (!json.isEmpty()) {
            try {
                Gson gson = new Gson();
                Type type = new TypeToken<List<Review>>() {}.getType();
                List<Review> allReviews = gson.fromJson(json, type);
                // Фильтруем по текущему месту
                for (Review review : allReviews) {
                    if (review.placeTitle.equals(placeTitle)) {
                        reviewsList.add(review);
                    }
                }
                // Сортируем от новых к старым
                Collections.sort(reviewsList, (a, b) -> Long.compare(b.timestamp, a.timestamp));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveReview(Review review) {
        if (prefs == null) return;

        Gson gson = new Gson();
        String json = prefs.getString(REVIEWS_KEY, "");
        List<Review> allReviews = new ArrayList<>();

        if (!json.isEmpty()) {
            Type type = new TypeToken<List<Review>>() {}.getType();
            allReviews = gson.fromJson(json, type);
        }

        allReviews.add(review);
        String newJson = gson.toJson(allReviews);
        prefs.edit().putString(REVIEWS_KEY, newJson).apply();

        // Обновляем список
        reviewsList.add(0, review);
        adapter.notifyDataSetChanged();
        updateStats();
    }

    private void updateStats() {
        if (reviewsList.isEmpty()) {
            tvAverageRating.setText("0.0");
            tvReviewsCount.setText("(0 отзывов)");
        } else {
            float sum = 0;
            for (Review r : reviewsList) {
                sum += r.rating;
            }
            float average = sum / reviewsList.size();
            tvAverageRating.setText(String.format("%.1f", average));
            tvReviewsCount.setText("(" + reviewsList.size() + " " + getReviewWord(reviewsList.size()) + ")");
        }
    }

    private String getReviewWord(int count) {
        if (count % 10 == 1 && count % 100 != 11) return "отзыв";
        if (count % 10 >= 2 && count % 10 <= 4 && (count % 100 < 10 || count % 100 >= 20)) return "отзыва";
        return "отзывов";
    }

    private void showAddReviewDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_add_review);
        dialog.setTitle("Добавить отзыв");

        EditText etUserName = dialog.findViewById(R.id.et_user_name);
        EditText etReviewText = dialog.findViewById(R.id.et_review_text);
        RatingBar rbRating = dialog.findViewById(R.id.rb_dialog_rating);
        Button btnSubmit = dialog.findViewById(R.id.btn_submit);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);

        // Загружаем сохранённое имя
        SharedPreferences userPrefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String savedName = userPrefs.getString("user_name", "");
        if (!savedName.isEmpty()) {
            etUserName.setText(savedName);
        }

        btnSubmit.setOnClickListener(v -> {
            String userName = etUserName.getText().toString().trim();
            String reviewText = etReviewText.getText().toString().trim();
            float rating = rbRating.getRating();

            if (TextUtils.isEmpty(userName)) {
                Toast.makeText(getContext(), "Введите ваше имя", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(reviewText)) {
                Toast.makeText(getContext(), "Введите текст отзыва", Toast.LENGTH_SHORT).show();
                return;
            }

            // Сохраняем имя пользователя
            userPrefs.edit().putString("user_name", userName).apply();

            Review review = new Review(placeTitle, userName, reviewText, rating);
            saveReview(review);

            Toast.makeText(getContext(), "Отзыв добавлен!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ViewHolder> {
        private List<Review> reviews;

        ReviewsAdapter(List<Review> reviews) {
            this.reviews = reviews;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_review, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
            Review r = reviews.get(pos);
            holder.tvAvatar.setText(r.userAvatar);
            holder.tvUserName.setText(r.userName);
            holder.tvDate.setText(r.getFormattedDate());
            holder.tvReviewText.setText(r.text);
            holder.rbRating.setRating(r.rating);
        }

        @Override
        public int getItemCount() {
            return reviews.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvAvatar, tvUserName, tvDate, tvReviewText;
            RatingBar rbRating;

            ViewHolder(@NonNull View v) {
                super(v);
                tvAvatar = v.findViewById(R.id.tv_avatar);
                tvUserName = v.findViewById(R.id.tv_user_name);
                tvDate = v.findViewById(R.id.tv_date);
                tvReviewText = v.findViewById(R.id.tv_review_text);
                rbRating = v.findViewById(R.id.rb_review);
            }
        }
    }
}