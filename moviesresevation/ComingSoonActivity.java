package com.example.moviesresevation;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.util.Log;
import android.widget.TextView;
import android.view.Gravity;
import androidx.appcompat.app.ActionBar;
import android.view.ViewGroup;
import android.view.View;

public class ComingSoonActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private static final String TAG = "ComingSoonActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coming_soon);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.comingSoonRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get coming soon movies from database
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        List<Movie> movies = dbHelper.getComingSoonMovies();

        if (movies.isEmpty()) {
            Log.w(TAG, "No coming soon movies found in database");
        }

        // Add debug logging for each movie
        for (Movie movie : movies) {
            Log.d(TAG, "Movie: " + movie.getTitle());
            Log.d(TAG, "Image URL: " + movie.getImageUrl());

            // Check if resource exists
            int resourceId = getResources().getIdentifier(
                    movie.getImageUrl(),
                    "drawable",
                    getPackageName()
            );

            if (resourceId == 0) {
                Log.e(TAG, "Image resource not found for: " + movie.getImageUrl());
            } else {
                Log.d(TAG, "Resource ID found: " + resourceId);
            }
        }

        // Set up adapter with error handling
        try {
            movieAdapter = new MovieAdapter(this, movies);
            recyclerView.setAdapter(movieAdapter);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up movie adapter: " + e.getMessage());
            // You might want to show an error message to the user here
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up any resources if needed
        if (movieAdapter != null) {
            movieAdapter = null;
        }
    }
} 