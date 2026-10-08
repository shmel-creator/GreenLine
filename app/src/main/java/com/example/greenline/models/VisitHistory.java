package com.example.greenline.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class VisitHistory {
    public String placeTitle;
    public String placeAddress;
    public double lat;
    public double lng;
    public long timestamp;
    public String actionType; // "route", "audio", "rating"

    public VisitHistory(String placeTitle, String placeAddress, double lat, double lng, String actionType) {
        this.placeTitle = placeTitle;
        this.placeAddress = placeAddress;
        this.lat = lat;
        this.lng = lng;
        this.timestamp = System.currentTimeMillis();
        this.actionType = actionType;
    }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public String getActionIcon() {
        switch (actionType) {
            case "route": return "🗺️";
            case "audio": return "🎧";
            case "rating": return "⭐";
            default: return "📍";
        }
    }
}