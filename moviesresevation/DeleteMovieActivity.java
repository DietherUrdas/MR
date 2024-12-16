package com.example.moviesresevation;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class DeleteMovieActivity extends AppCompatActivity {
    private ListView movieListView;
    private DatabaseHelper dbHelper;
    private List<Movie> movies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_movie);

        dbHelper = new DatabaseHelper(this);
        movieListView = findViewById(R.id.movieListView);

        loadMovies();

        movieListView.setOnItemClickListener((parent, view, position, id) -> {
            Movie selectedMovie = movies.get(position);
            showDeleteConfirmation(selectedMovie);
        });
    }

    private void loadMovies() {
        movies = dbHelper.getAllMovies();
        ArrayAdapter<Movie> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_list_item_1, movies);
        movieListView.setAdapter(adapter);
    }

    private void showDeleteConfirmation(Movie movie) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_delete_movie, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView titleText = dialogView.findViewById(R.id.dialog_title);
        TextView messageText = dialogView.findViewById(R.id.dialog_message);
        Button cancelButton = dialogView.findViewById(R.id.btn_cancel);
        Button deleteButton = dialogView.findViewById(R.id.btn_delete);

        messageText.setText("Are you sure you want to delete " + movie.getTitle() + "?");

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        deleteButton.setOnClickListener(v -> {
            boolean success = dbHelper.deleteMovie(movie.getId());
            if (success) {
                Toast.makeText(this, "Movie deleted successfully", Toast.LENGTH_SHORT).show();
                loadMovies();
            } else {
                Toast.makeText(this, "Failed to delete movie", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });

        dialog.show();
    }
}
