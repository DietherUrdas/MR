package com.example.moviesresevation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Button;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class MovieListActivity extends AppCompatActivity {
    private RecyclerView moviesRecyclerView;
    private MovieAdapter movieAdapter;
    private DatabaseHelper dbHelper;
    private List<Movie> allMovies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Now Showing");

        // Set up RecyclerView
        moviesRecyclerView = findViewById(R.id.moviesRecyclerView);
        moviesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load movies and set up adapter
        allMovies = dbHelper.getAllMovies();
        movieAdapter = new MovieAdapter(this, allMovies);
        moviesRecyclerView.setAdapter(movieAdapter);

        // Set up search functionality
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterMovies(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterMovies(newText);
                return true;
            }
        });

        // Set up Coming Soon button
        Button comingSoonButton = findViewById(R.id.comingSoonButton);
        comingSoonButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ComingSoonActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onBackPressed() {
        showLogoutDialog();
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_logout, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);

        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        MaterialButton btnLogout = dialogView.findViewById(R.id.btnLogout);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnLogout.setOnClickListener(v -> {
            // Clear user session
            SharedPreferences prefs = getSharedPreferences("MovieApp", MODE_PRIVATE);
            prefs.edit().clear().apply();

            // Return to login screen
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }

    private void filterMovies(String query) {
        List<Movie> filteredMovies = new ArrayList<>();
        for (Movie movie : allMovies) {
            if (movie.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredMovies.add(movie);
            }
        }
        movieAdapter.updateMovies(filteredMovies);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh movies list when returning to this activity
        allMovies = dbHelper.getAllMovies();
        movieAdapter.updateMovies(allMovies);
    }
} 