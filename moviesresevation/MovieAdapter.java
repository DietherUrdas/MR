package com.example.moviesresevation;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private List<Movie> movies;
    private Context context;

    public MovieAdapter(Context context, List<Movie> movies) {
        this.context = context;
        this.movies = movies;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);

        // Add this debug log
        Log.d("MovieAdapter", "Loading image: " + movie.getImageUrl());

        // Get the resource ID
        int imageResId = context.getResources().getIdentifier(
                movie.getImageUrl(),
                "drawable",
                context.getPackageName()
        );

        // Add this debug log
        Log.d("MovieAdapter", "Resource ID: " + imageResId);

        holder.bind(movie);

        // Set click listener for the entire item
        holder.itemView.setOnClickListener(v -> showTrailerDialog(movie));
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView movieImage;
        TextView movieTitle, movieDescription;
        MaterialButton bookButton;

        MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            movieImage = itemView.findViewById(R.id.movieImage);
            movieTitle = itemView.findViewById(R.id.movieTitle);
            movieDescription = itemView.findViewById(R.id.movieDescription);
            bookButton = itemView.findViewById(R.id.bookButton);
        }

        void bind(final Movie movie) {
            movieTitle.setText(movie.getTitle());
            movieDescription.setText(movie.getDescription());

            // Set movie image
            switch (movie.getTitle()) {
                case "Hello, Love, Again":
                    movieImage.setImageResource(R.drawable.hello_love);
                    break;
                case "Moana 2":
                    movieImage.setImageResource(R.drawable.moana);
                    break;
                case "Wicked":
                    movieImage.setImageResource(R.drawable.wicked);
                    break;
                case "Gladiator II":
                    movieImage.setImageResource(R.drawable.gladiator);
                    break;
                case "Heretic":
                    movieImage.setImageResource(R.drawable.heretic);
                    break;
                case "Mufasa: The Lion King":
                    movieImage.setImageResource(R.drawable.mufasa_poster);
                    break;
                case "Sonic the Hedgehog 3":
                    movieImage.setImageResource(R.drawable.sonic_poster);
                    break;
                case "Better Man":
                    movieImage.setImageResource(R.drawable.betterman_poster);
                    break;
                case "Kraven the Hunter":
                    movieImage.setImageResource(R.drawable.kraven_poster);
                    break;
                default:
                    movieImage.setImageResource(R.drawable.app_logo);
                    Log.w("MovieAdapter", "No image found for movie: " + movie.getTitle());
                    break;
            }

            // Set price on button
            if (context instanceof ComingSoonActivity) {
                // For coming soon movies, change the button text and disable it
                bookButton.setText("Coming Soon");
                bookButton.setEnabled(false);
                bookButton.setBackgroundColor(Color.parseColor("#999999"));
            } else {
                // For regular movies, show the price and enable booking
                bookButton.setText(String.format("Book Now ₱%.2f", movie.getPrice()));
                bookButton.setEnabled(true);
                
                // Set click listener for book button
                bookButton.setOnClickListener(v -> {
                    Intent intent = new Intent(context, SeatReservationActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("movieId", movie.getId());
                    intent.putExtra("movieTitle", movie.getTitle());
                    intent.putExtra("moviePrice", movie.getPrice());
                    context.startActivity(intent);
                });
            }
        }
    }

    public void updateMovies(List<Movie> newMovies) {
        this.movies = newMovies;
        notifyDataSetChanged();
    }

    private void showTrailerDialog(Movie movie) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_trailer, null);
        builder.setView(dialogView);

        VideoView videoView = dialogView.findViewById(R.id.videoView);
        TextView titleText = dialogView.findViewById(R.id.titleText);
        MaterialButton closeButton = dialogView.findViewById(R.id.closeButton);

        titleText.setText(movie.getTitle() + " - Trailer");
        titleText.setTextColor(context.getResources().getColor(android.R.color.white));

        // Set up video with specific trailer resource
        String videoPath = "android.resource://" + context.getPackageName() + "/" + movie.getTrailerResource();
        videoView.setVideoURI(Uri.parse(videoPath));

        // Create media controller
        MediaController mediaController = new MediaController(context);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Set up video listeners
        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            videoView.start();
        });

        videoView.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(context, "Error playing trailer", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return true;
        });

        // Handle dialog dismiss
        dialog.setOnDismissListener(dialogInterface -> {
            videoView.stopPlayback();
        });

        // Handle close button click
        closeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}