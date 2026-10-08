package com.example.greenline.models;

public class Review {
    public String placeTitle;
    public String userName;
    public String text;
    public float rating;
    public long timestamp;
    public String userAvatar; // первая буква имени

    public Review(String placeTitle, String userName, String text, float rating) {
        this.placeTitle = placeTitle;
        this.userName = userName;
        this.text = text;
        this.rating = rating;
        this.timestamp = System.currentTimeMillis();
        this.userAvatar = userName.substring(0, 1).toUpperCase();
    }

    public String getFormattedDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(timestamp));
    }
}