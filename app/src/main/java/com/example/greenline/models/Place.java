package com.example.greenline.models;

public class Place {
    public String title;
    public String address;
    public double lat;
    public double lng;
    public double rating;
    public String description;
    public int audioRes;

    public Place(String title, String address, double lat, double lng, double rating, String description, int audioRes) {
        this.title = title;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.rating = rating;
        this.description = description;
        this.audioRes = audioRes;
    }

    public Place(String title, String address, double lat, double lng, double rating) {
        this(title, address, lat, lng, rating, "", 0);
    }
}