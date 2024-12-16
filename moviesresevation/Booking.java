package com.example.moviesresevation;

public class Booking {
    private int id;
    private String movieTitle;
    private String userName;
    private String bookingDate;
    private int movieId;
    private int userId;
    private String seats;
    private double totalPrice;

    public Booking(int id, String movieTitle, String userName, String bookingDate, int movieId, int userId, String seats, double totalPrice) {
        this.id = id;
        this.movieTitle = movieTitle;
        this.userName = userName;
        this.bookingDate = bookingDate;
        this.movieId = movieId;
        this.userId = userId;
        this.seats = seats;
        this.totalPrice = totalPrice;
    }

    // Getters
    public int getId() { return id; }
    public String getMovieTitle() { return movieTitle; }
    public String getUserName() { return userName; }
    public String getBookingDate() { return bookingDate; }
    public int getMovieId() { return movieId; }
    public int getUserId() { return userId; }
    public String getSeats() { return seats; }
    public double getTotalPrice() { return totalPrice; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }
    public void setMovieId(int movieId) { this.movieId = movieId; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setSeats(String seats) { this.seats = seats; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
}