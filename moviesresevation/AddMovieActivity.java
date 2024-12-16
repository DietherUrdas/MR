package com.example.moviesresevation;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddMovieActivity extends AppCompatActivity {
    private EditText titleInput, descriptionInput, priceInput, imageUrlInput;
    private Button submitButton;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_movie);

        dbHelper = new DatabaseHelper(this);

        // Initialize views
        titleInput = findViewById(R.id.titleInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        priceInput = findViewById(R.id.priceInput);
        imageUrlInput = findViewById(R.id.imageUrlInput);
        submitButton = findViewById(R.id.submitButton);

        submitButton.setOnClickListener(v -> addMovie());
    }

    private void addMovie() {
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
            // Create movie with default values for id and rating
            Movie movie = new Movie(0, title, description, imageUrl, "", price, 0.0, 0);

            long result = dbHelper.insertMovie(movie);
            if (result != -1) {
                Toast.makeText(this, "Movie added successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to add movie", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show();
        }
    }
}
