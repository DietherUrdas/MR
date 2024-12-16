package com.example.moviesresevation;

public class Movie {
    private int id;
    private String title;
    private String description;
    private String duration;
    private String imageUrl;
    private double price;
    private double rating;
    private int trailerResource;
    private boolean isComingSoon;

    public Movie(int id, String title, String description, String duration, String imageUrl, double price, double rating, int trailerResource) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.imageUrl = imageUrl;
        this.price = price;
        this.rating = rating;
        this.trailerResource = trailerResource;
        this.isComingSoon = false;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDuration() { return duration; }
    public String getImageUrl() { return imageUrl; }
    public double getPrice() { return price; }
    public double getRating() { return rating; }
    public int getTrailerResource() { return trailerResource; }
    public boolean isComingSoon() { return isComingSoon; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDuration(String duration) { this.duration = duration; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setPrice(double price) { this.price = price; }
    public void setRating(double rating) { this.rating = rating; }
    public void setTrailerResource(int trailerResource) { this.trailerResource = trailerResource; }
    public void setComingSoon(boolean isComingSoon) { this.isComingSoon = isComingSoon; }
}