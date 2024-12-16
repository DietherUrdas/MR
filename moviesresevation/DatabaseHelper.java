package com.example.moviesresevation;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "movies_reservation.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // users table
        db.execSQL("CREATE TABLE users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "username TEXT UNIQUE,"
                + "password TEXT,"
                + "security_question TEXT,"
                + "security_answer TEXT)");

        // movies table with all required fields including is_coming_soon
        db.execSQL("CREATE TABLE movies ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "title TEXT,"
                + "description TEXT,"
                + "duration TEXT,"
                + "image_url TEXT,"
                + "price REAL,"
                + "rating REAL,"
                + "trailer_resource INTEGER,"
                + "is_coming_soon INTEGER DEFAULT 0)");

        // reservations table with all fields
        db.execSQL("CREATE TABLE reservations ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "username TEXT,"
                + "movie_id INTEGER,"
                + "seat_number TEXT,"
                + "show_time TEXT,"
                + "payment_method TEXT DEFAULT '',"
                + "payment_status TEXT DEFAULT 'Pending',"
                + "payment_details TEXT DEFAULT '',"
                + "total_price REAL DEFAULT 0.0,"
                + "reservation_date DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(movie_id) REFERENCES movies(id),"
                + "UNIQUE(movie_id, seat_number, show_time))");

        // history_log table
        db.execSQL("CREATE TABLE IF NOT EXISTS history_log ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "action_type TEXT,"
                + "table_name TEXT,"
                + "record_id TEXT,"
                + "details TEXT,"
                + "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");

        // sample movies and coming soon movies
        addSampleMovies(db);
        addComingSoonMovies(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop existing tables
        db.execSQL("DROP TABLE IF EXISTS history_log");
        db.execSQL("DROP TABLE IF EXISTS reservations");
        db.execSQL("DROP TABLE IF EXISTS movies");
        db.execSQL("DROP TABLE IF EXISTS users");

        // Recreate tables
        onCreate(db);
    }

    private void addSampleMovies(SQLiteDatabase db) {
        // Check if movies already exist
        SQLiteCursor sqliteQuery = (SQLiteCursor) db.rawQuery("SELECT COUNT(*) FROM movies", null);
        sqliteQuery.moveToFirst();
        int count = sqliteQuery.getInt(0);
        sqliteQuery.close();

        if (count == 0) {
            ContentValues[] movies = {
                    createMovie("Hello, Love, Again", "A romantic story about second chances and rekindling lost love", "2h 5min", "hello_love", 440.0, 4.5, R.raw.hello_love_trailer),
                    createMovie("Moana 2", "Continue the adventure with Moana in this exciting Disney sequel", "1h 55min", "moana", 450.0, 4.8, R.raw.moana_trailer),
                    createMovie("Wicked", "The untold story of the Witches of Oz comes to life in this magical adaptation", "2h 30min", "wicked", 350.0, 4.3, R.raw.wicked_trailer),
                    createMovie("Gladiator II", "The epic continuation of the legendary tale of strength and revenge", "2h 45min", "gladiator", 450.0, 4.6, R.raw.gladiator_trailer),
                    createMovie("Heretic", "A thrilling supernatural horror that will keep you on the edge of your seat", "1h 50min", "heretic", 440.0, 4.2, R.raw.heretic_trailer)
            };

            db.beginTransaction();
            try {
                for (ContentValues movie : movies) {
                    long result = db.insert("movies", null, movie);
                    if (result == -1) {
                        throw new Exception("Failed to insert movie");
                    }
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
            }
        }
    }

    private void addComingSoonMovies(SQLiteDatabase db) {
        ContentValues[] comingSoonMovies = {
                createMovie(
                        "Mufasa: The Lion King",
                        "Told through the eyes of Rafiki, Mufasa: The Lion King chronicles the rise of one of the greatest kings of the Pride Lands.",
                        "2h 15min",
                        "mufasa_poster",
                        450.0,
                        4.8,
                        R.raw.mufasa_trailer
                ),
                createMovie(
                        "Sonic the Hedgehog 3",
                        "The world's fastest hedgehog teams up with Shadow in an epic adventure that will test their speed and friendship.",
                        "2h 0min",
                        "sonic_poster",
                        440.0,
                        4.7,
                        R.raw.sonic_trailer
                ),
                createMovie(
                        "Better Man",
                        "A musical biopic that tells the story of Robbie Williams' rise to fame and the demons he battled both on and off stage.",
                        "2h 20min",
                        "betterman_poster",
                        430.0,
                        4.6,
                        R.raw.betterman_trailer
                ),
                createMovie(
                        "Kraven the Hunter",
                        "Russian immigrant Sergei Kravinoff sets out to prove that he is the greatest hunter in the world.",
                        "2h 5min",
                        "kraven_poster",
                        450.0,
                        4.5,
                        R.raw.kraven_trailer
                )
        };

        db.beginTransaction();
        try {
            for (ContentValues movie : comingSoonMovies) {
                movie.put("is_coming_soon", 1);
                long result = db.insert("movies", null, movie);
                if (result == -1) {
                    throw new Exception("Failed to insert coming soon movie");
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    private ContentValues createMovie(String title, String description, String duration, String imageUrl, double price, double rating, int trailerResource) {
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("description", description);
        values.put("duration", duration);
        values.put("image_url", imageUrl);
        values.put("price", price);
        values.put("rating", rating);
        values.put("trailer_resource", trailerResource);
        return values;
    }

    private void logAction(String actionType, String tableName, String recordId, String details) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("action_type", actionType);
            values.put("table_name", tableName);
            values.put("record_id", recordId);
            values.put("details", details);

            long result = db.insert("history_log", null, values);
            Log.d("DatabaseHelper", "Logged action: " + actionType + " with result: " + result);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error logging action: " + e.getMessage());
        }
    }

    public boolean addReservation(String username, int movieId, String seatNumber,
                                  String showTime, String paymentMethod, String paymentStatus,
                                  String paymentDetails, double price) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("movie_id", movieId);
        values.put("seat_number", seatNumber);
        values.put("show_time", showTime);
        values.put("payment_method", paymentMethod);
        values.put("payment_status", paymentStatus);
        values.put("payment_details", paymentDetails);
        values.put("total_price", price);

        try {
            long result = db.insertOrThrow("reservations", null, values);
            if (result != -1) {
                SQLiteCursor sqliteQuery = (SQLiteCursor) db.query("movies",
                        new String[]{"title"},
                        "id = ?",
                        new String[]{String.valueOf(movieId)},
                        null, null, null);

                String movieTitle = "";
                if (sqliteQuery != null && sqliteQuery.moveToFirst()) {
                    movieTitle = sqliteQuery.getString(sqliteQuery.getColumnIndex("title"));
                    sqliteQuery.close();
                }

                logAction("RESERVE",
                        "reservations",
                        String.valueOf(result),
                        "User: " + username +
                                "\nReserved Movie: " + movieTitle +
                                " (" + showTime + ")" +
                                "\nSeats: " + seatNumber +
                                "\nTotal: ₱" + price);
            }
            return result != -1;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error adding reservation: " + e.getMessage());
            return false;
        }
    }

    public boolean addReservation(String username, int movieId, String seatNumber, String showTime) {
        return addReservation(username, movieId, seatNumber, showTime,
                "", "Pending", "", 0.0);
    }

    public boolean updateReservationPayment(String username, int movieId, String seatNumber, String showTime, String paymentMethod, String paymentStatus, String paymentDetails, double price) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("payment_method", paymentMethod);
        values.put("payment_status", paymentStatus);
        values.put("payment_details", paymentDetails);
        values.put("total_price", price);

        String whereClause = "username=? AND movie_id=? AND seat_number=? AND show_time=?";
        String[] whereArgs = {username, String.valueOf(movieId), seatNumber, showTime};

        int result = db.update("reservations", values, whereClause, whereArgs);
        return result > 0;
    }

    public List<String> getReservedSeats(int movieId, String showTime) {
        List<String> seats = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT seat_number FROM reservations " +
                "WHERE movie_id = ? AND show_time = ? " +
                "AND (payment_status != 'Cancelled' OR payment_status IS NULL)";

        SQLiteCursor sqliteQuery = (SQLiteCursor) db.rawQuery(query, new String[]{String.valueOf(movieId), showTime});

        while (sqliteQuery.moveToNext()) {
            seats.add(sqliteQuery.getString(0));
        }
        sqliteQuery.close();
        return seats;
    }

    public List<String> getUserReservations(String username, int movieId, String showTime) {
        List<String> seats = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT seat_number FROM reservations " +
                "WHERE username = ? AND movie_id = ? AND show_time = ? " +
                "AND payment_status != 'Cancelled'";

        SQLiteCursor sqliteQuery = (SQLiteCursor) db.rawQuery(query,
                new String[]{username, String.valueOf(movieId), showTime});

        while (sqliteQuery.moveToNext()) {
            seats.add(sqliteQuery.getString(0));
        }
        sqliteQuery.close();
        return seats;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            SQLiteCursor sqliteQuery = (SQLiteCursor) db.rawQuery(
                    "SELECT * FROM users WHERE username = ? AND password = ?",
                    new String[]{username, password}
            );
            boolean exists = sqliteQuery.getCount() > 0;
            sqliteQuery.close();
            return exists;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error checking user: " + e.getMessage());
            return false;
        }
    }

    public boolean isUsernameTaken(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            SQLiteCursor sqliteQuery = (SQLiteCursor) db.rawQuery(
                    "SELECT * FROM users WHERE username = ?",
                    new String[]{username}
            );
            boolean exists = sqliteQuery.getCount() > 0;
            sqliteQuery.close();
            return exists;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error checking username: " + e.getMessage());
            return true;
        }
    }

    public boolean addUser(String username, String password, String securityQuestion, String securityAnswer) {
        Log.d("DatabaseHelper", "Attempting to add user: " + username);

        SQLiteDatabase db = this.getReadableDatabase();

        try {
            SQLiteCursor sqliteQuery = (SQLiteCursor) db.rawQuery("SELECT * FROM users WHERE username = ?",
                    new String[]{username});

            Log.d("DatabaseHelper", "Found existing users: " + sqliteQuery.getCount());

            if (sqliteQuery.getCount() > 0) {
                sqliteQuery.close();
                Log.d("DatabaseHelper", "Username already exists: " + username);
                return false;
            }
            sqliteQuery.close();

            ContentValues values = new ContentValues();
            values.put("username", username);
            values.put("password", password);
            values.put("security_question", securityQuestion);
            values.put("security_answer", securityAnswer);

            long result = db.insert("users", null, values);

            Log.d("DatabaseHelper", "Insert result: " + result);

            return result != -1;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error adding user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUserPassword(String username, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", newPassword);

        try {
            int result = db.update("users", values, "username = ?",
                    new String[]{username});
            return result > 0;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error updating password: " + e.getMessage());
            return false;
        }
    }

    private void logDeleteAction(String tableName, String recordId, String details) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("action_type", "DELETE");
            values.put("table_name", tableName);
            values.put("record_id", recordId);
            values.put("details", details);

            long result = db.insert("history_log", null, values);
            Log.d("DatabaseHelper", "Logged delete action with result: " + result);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error logging delete action: " + e.getMessage());
        }
    }

    public boolean deleteUser(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            logDeleteAction("users", username, "Deleting user and their reservations");

            int reservationsDeleted = db.delete("reservations", "username = ?",
                    new String[]{username});

            int result = db.delete("users", "username = ?", new String[]{username});

            return result > 0;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting user: " + e.getMessage());
            return false;
        }
    }

    public List<HistoryLog> getDeleteHistory() {
        List<HistoryLog> historyLogs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            SQLiteCursor sqliteQuery = (SQLiteCursor) db.query("history_log",
                    null,
                    null,
                    null,
                    null,
                    null,
                    "timestamp DESC");

            if (sqliteQuery != null && sqliteQuery.moveToFirst()) {
                do {
                    HistoryLog log = new HistoryLog(
                            sqliteQuery.getInt(sqliteQuery.getColumnIndex("id")),
                            sqliteQuery.getString(sqliteQuery.getColumnIndex("action_type")),
                            sqliteQuery.getString(sqliteQuery.getColumnIndex("table_name")),
                            sqliteQuery.getString(sqliteQuery.getColumnIndex("record_id")),
                            sqliteQuery.getString(sqliteQuery.getColumnIndex("details")),
                            sqliteQuery.getString(sqliteQuery.getColumnIndex("timestamp"))
                    );
                    historyLogs.add(log);
                } while (sqliteQuery.moveToNext());
                sqliteQuery.close();
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting history: " + e.getMessage());
        }

        return historyLogs;
    }

    public SQLiteCursor getUserDetails(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        return (SQLiteCursor) db.query("users",
                new String[]{"id", "username"},
                "username = ?",
                new String[]{username},
                null, null, null);
    }

    public static boolean isValidUsername(String username) {
        return username != null &&
                username.trim().length() >= 3 &&
                username.trim().length() <= 20 &&
                username.matches("^[a-zA-Z0-9_]+$");
    }

    public static boolean isValidPassword(String password) {
        return password != null &&
                password.trim().length() >= 6 &&
                password.trim().length() <= 20 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*");
    }

    public List<Movie> getAllMovies() {
        List<Movie> movies = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        SQLiteCursor sqliteQuery = (SQLiteCursor) db.query("movies",
                null,
                "is_coming_soon = ?",
                new String[]{"0"},
                null, null, null);

        if (sqliteQuery.moveToFirst()) {
            do {
                movies.add(new Movie(
                        sqliteQuery.getInt(sqliteQuery.getColumnIndex("id")),
                        sqliteQuery.getString(sqliteQuery.getColumnIndex("title")),
                        sqliteQuery.getString(sqliteQuery.getColumnIndex("description")),
                        sqliteQuery.getString(sqliteQuery.getColumnIndex("duration")),
                        sqliteQuery.getString(sqliteQuery.getColumnIndex("image_url")),
                        sqliteQuery.getDouble(sqliteQuery.getColumnIndex("price")),
                        sqliteQuery.getDouble(sqliteQuery.getColumnIndex("rating")),
                        sqliteQuery.getInt(sqliteQuery.getColumnIndex("trailer_resource"))
                ));
            } while (sqliteQuery.moveToNext());
        }
        sqliteQuery.close();
        return movies;
    }

    public Movie getMovie(int movieId) {
        SQLiteDatabase db = this.getReadableDatabase();
        SQLiteCursor sqliteQuery = (SQLiteCursor) db.query("movies",
                null,
                "id = ?",
                new String[]{String.valueOf(movieId)},
                null,
                null,
                null);

        Movie movie = null;
        if (sqliteQuery.moveToFirst()) {
            movie = new Movie(
                    sqliteQuery.getInt(sqliteQuery.getColumnIndex("id")),
                    sqliteQuery.getString(sqliteQuery.getColumnIndex("title")),
                    sqliteQuery.getString(sqliteQuery.getColumnIndex("description")),
                    sqliteQuery.getString(sqliteQuery.getColumnIndex("duration")),
                    sqliteQuery.getString(sqliteQuery.getColumnIndex("image_url")),
                    sqliteQuery.getDouble(sqliteQuery.getColumnIndex("price")),
                    sqliteQuery.getDouble(sqliteQuery.getColumnIndex("rating")),
                    sqliteQuery.getInt(sqliteQuery.getColumnIndex("trailer_resource"))
            );
        }
        sqliteQuery.close();
        return movie;
    }

    public void logDatabaseContents() {
        SQLiteDatabase db = this.getReadableDatabase();

        SQLiteCursor sqliteQuery = (SQLiteCursor) db.rawQuery("SELECT * FROM history_log", null);
        Log.d("DatabaseDebug", "Number of history logs: " + sqliteQuery.getCount());

        if (sqliteQuery.moveToFirst()) {
            do {
                String action = sqliteQuery.getString(sqliteQuery.getColumnIndex("action_type"));
                String details = sqliteQuery.getString(sqliteQuery.getColumnIndex("details"));
                Log.d("DatabaseDebug", "History Log: " + action + " - " + details);
            } while (sqliteQuery.moveToNext());
        }
        sqliteQuery.close();
    }

    public List<Movie> getComingSoonMovies() {
        List<Movie> movies = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        SQLiteCursor sqliteQuery = (SQLiteCursor) db.query("movies",
                null,
                "is_coming_soon = ?",
                new String[]{"1"},
                null, null, null);

        if (sqliteQuery.moveToFirst()) {
            do {
                movies.add(new Movie(
                        sqliteQuery.getInt(sqliteQuery.getColumnIndex("id")),
                        sqliteQuery.getString(sqliteQuery.getColumnIndex("title")),
                        sqliteQuery.getString(sqliteQuery.getColumnIndex("description")),
                        sqliteQuery.getString(sqliteQuery.getColumnIndex("duration")),
                        sqliteQuery.getString(sqliteQuery.getColumnIndex("image_url")),
                        sqliteQuery.getDouble(sqliteQuery.getColumnIndex("price")),
                        sqliteQuery.getDouble(sqliteQuery.getColumnIndex("rating")),
                        sqliteQuery.getInt(sqliteQuery.getColumnIndex("trailer_resource"))
                ));
            } while (sqliteQuery.moveToNext());
        }
        sqliteQuery.close();
        return movies;
    }

    public boolean verifySecurityAnswer(String username, String securityAnswer) {
        SQLiteDatabase db = this.getReadableDatabase();
        SQLiteCursor sqliteQuery = (SQLiteCursor) db.query("users", new String[]{"security_answer"},
                "username = ?", new String[]{username},
                null, null, null);

        if (sqliteQuery != null && sqliteQuery.moveToFirst()) {
            String storedAnswer = sqliteQuery.getString(0);
            sqliteQuery.close();
            return storedAnswer.equals(securityAnswer);
        }
        return false;
    }

    public boolean updatePassword(String username, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", newPassword);

        int rowsAffected = db.update("users", values,
                "username = ?", new String[]{username});
        return rowsAffected > 0;
    }

    public void logAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        SQLiteCursor sqliteQuery = (SQLiteCursor) db.rawQuery("SELECT * FROM users", null);

        Log.d("DatabaseHelper", "Total users in database: " + sqliteQuery.getCount());

        if (sqliteQuery.moveToFirst()) {
            do {
                String username = sqliteQuery.getString(sqliteQuery.getColumnIndex("username"));
                Log.d("DatabaseHelper", "User found: " + username);
            } while (sqliteQuery.moveToNext());
        }
        sqliteQuery.close();
    }

    public void resetDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
        Log.d("DatabaseHelper", "Database reset completed");
    }

    public boolean deleteHistoryLog(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            int result = db.delete("history_log", "id = ?",
                    new String[]{String.valueOf(id)});
            return result > 0;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting history log: " + e.getMessage());
            return false;
        }
    }

    public long insertMovie(Movie movie) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("title", movie.getTitle());
        values.put("description", movie.getDescription());
        values.put("duration", movie.getDuration());
        values.put("image_url", movie.getImageUrl());
        values.put("price", movie.getPrice());
        values.put("rating", movie.getRating());
        values.put("trailer_resource", movie.getTrailerResource());
        values.put("is_coming_soon", movie.isComingSoon() ? 1 : 0);

        return db.insert("movies", null, values);
    }

    public boolean deleteMovie(int movieId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete("movies", "id = ?", new String[]{String.valueOf(movieId)});
        db.close();
        return rowsDeleted > 0;
    }

    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT r.id, m.title as movieTitle, r.username, r.reservation_date, " +
                "r.movie_id, r.user_id, r.seat_number, r.total_price " +
                "FROM reservations r " +
                "JOIN movies m ON r.movie_id = m.id";
        SQLiteCursor sqliteQuery = (SQLiteCursor) db.rawQuery(query, null);

        if (sqliteQuery.moveToFirst()) {
            do {
                int id = sqliteQuery.getInt(sqliteQuery.getColumnIndex("id"));
                String movieTitle = sqliteQuery.getString(sqliteQuery.getColumnIndex("movieTitle"));
                String userName = sqliteQuery.getString(sqliteQuery.getColumnIndex("username"));
                String bookingDate = sqliteQuery.getString(sqliteQuery.getColumnIndex("reservation_date"));

                bookings.add(new Booking(
                        id,
                        movieTitle,
                        userName,
                        bookingDate,
                        sqliteQuery.getInt(sqliteQuery.getColumnIndex("movie_id")),
                        sqliteQuery.getInt(sqliteQuery.getColumnIndex("user_id")),
                        sqliteQuery.getString(sqliteQuery.getColumnIndex("seat_number")),
                        sqliteQuery.getDouble(sqliteQuery.getColumnIndex("total_price"))
                ));
            } while (sqliteQuery.moveToNext());
        }

        sqliteQuery.close();
        return bookings;
    }

    public boolean updateMovie(Movie movie) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", movie.getTitle());
        values.put("description", movie.getDescription());
        values.put("duration", movie.getDuration());
        values.put("image_url", movie.getImageUrl());
        values.put("price", movie.getPrice());
        values.put("rating", movie.getRating());
        values.put("trailer_resource", movie.getTrailerResource());
        values.put("is_coming_soon", movie.isComingSoon() ? 1 : 0);

        int rowsAffected = db.update("movies", values, "id = ?",
                new String[]{String.valueOf(movie.getId())});
        return rowsAffected > 0;
    }

    public String getUsernameById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String username = null;
        SQLiteCursor sqliteQuery = (SQLiteCursor) db.query("users", new String[]{"username"}, "id = ?",
                new String[]{String.valueOf(userId)}, null, null, null);
        if (sqliteQuery.moveToFirst()) {
            username = sqliteQuery.getString(0);
        }
        sqliteQuery.close();
        return username;
    }
}