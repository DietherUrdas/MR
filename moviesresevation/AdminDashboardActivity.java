package com.example.moviesresevation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class AdminDashboardActivity extends AppCompatActivity {
    private Button addMovieBtn, editMovieBtn, deleteMovieBtn, viewBookingsBtn, logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize views with proper casting
        addMovieBtn = findViewById(R.id.addMovieBtn);
        editMovieBtn = findViewById(R.id.editMovieBtn);
        deleteMovieBtn = findViewById(R.id.deleteMovieBtn);
        viewBookingsBtn = findViewById(R.id.viewBookingsBtn);
        logoutBtn = findViewById(R.id.logoutBtn);

        // Set click listeners
        addMovieBtn.setOnClickListener(v -> startActivity(new Intent(this, AddMovieActivity.class)));
        editMovieBtn.setOnClickListener(v -> startActivity(new Intent(this, EditMovieActivity.class)));
        deleteMovieBtn.setOnClickListener(v -> startActivity(new Intent(this, DeleteMovieActivity.class)));
        viewBookingsBtn.setOnClickListener(v -> startActivity(new Intent(this, HistoryLogActivity.class)));

        logoutBtn.setOnClickListener(v -> {
            // Show logout dialog
            showLogoutDialog();
        });
    }

    @Override
    public void onBackPressed() {
        showLogoutDialog(); // Show logout dialog instead of the default back action
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

        // Handle cancel button click
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Handle logout button click
        btnLogout.setOnClickListener(v -> {
            // Clear user session from SharedPreferences
            SharedPreferences prefs = getSharedPreferences("MovieApp", MODE_PRIVATE);
            prefs.edit().clear().apply();

            // Return to login screen after clearing session
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Finish current activity to prevent going back to admin dashboard
        });

        dialog.show();
    }
}
