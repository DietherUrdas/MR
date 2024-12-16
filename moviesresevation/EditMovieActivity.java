package com.example.moviesresevation;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class EditMovieActivity extends AppCompatActivity {
    private Spinner movieSpinner;
    private EditText titleInput, descriptionInput, priceInput, imageUrlInput;
    private Button updateButton;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_movie);

        dbHelper = new DatabaseHelper(this);

        // Initialize views
        movieSpinner = findViewById(R.id.movieSpinner);
        titleInput = findViewById(R.id.titleInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        priceInput = findViewById(R.id.priceInput);
        imageUrlInput = findViewById(R.id.imageUrlInput);
        updateButton = findViewById(R.id.updateButton);

        loadMovies();

        movieSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Movie selectedMovie = (Movie) parent.getItemAtPosition(position);
                populateFields(selectedMovie);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        updateButton.setOnClickListener(v -> updateMovie());
    }

    private void loadMovies() {
        List<Movie> movies = dbHelper.getAllMovies();
        ArrayAdapter<Movie> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, movies);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        movieSpinner.setAdapter(adapter);
    }

    private void populateFields(Movie movie) {
        titleInput.setText(movie.getTitle());
        descriptionInput.setText(movie.getDescription());
        priceInput.setText(String.valueOf(movie.getPrice()));
        imageUrlInput.setText(movie.getImageUrl());
    }

    private void updateMovie() {
        Movie selectedMovie = (Movie) movieSpinner.getSelectedItem();
        if (selectedMovie == null) return;

        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String priceStr = priceInput.getText().toString().trim();
        String imageUrl = imageUrlInput.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            selectedMovie.setTitle(title);
            selectedMovie.setDescription(description);
            selectedMovie.setPrice(price);
            selectedMovie.setImageUrl(imageUrl);

            boolean success = dbHelper.updateMovie(selectedMovie);
            if (success) {
                Toast.makeText(this, "Movie updated successfully", Toast.LENGTH_SHORT).show();
                loadMovies(); // Refresh the spinner
            } else {
                Toast.makeText(this, "Failed to update movie", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show();
        }
    }
}
