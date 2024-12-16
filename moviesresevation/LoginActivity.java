package com.example.moviesresevation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText usernameInput, passwordInput;
    private Button loginButton, registerButton, forgotPasswordButton;
    private DatabaseHelper dbHelper;
    private ImageView animationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);

        // Initialize views
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        animationView = findViewById(R.id.animationView);
        forgotPasswordButton = findViewById(R.id.forgotPasswordButton);

        loginButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString();
            String password = passwordInput.getText().toString();

            attemptLogin(username, password);
        });
        registerButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        forgotPasswordButton.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void attemptLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check for admin login
        if (username.equals("admin") && password.equals("admin123")) {
            // Save admin login state
            SharedPreferences prefs = getSharedPreferences("MovieApp", MODE_PRIVATE);
            prefs.edit()
                .putString("username", username)
                .putBoolean("isAdmin", true)
                .apply();

            // Redirect to admin dashboard
            startActivity(new Intent(this, AdminDashboardActivity.class));
            finish();
            return;
        }

        // Regular user login
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        if (dbHelper.checkUser(username, password)) {
            // Save login state
            SharedPreferences prefs = getSharedPreferences("MovieApp", MODE_PRIVATE);
            prefs.edit()
                .putString("username", username)
                .putBoolean("isAdmin", false)
                .apply();

            // Check if we need to return to movie booking
            if (getIntent().getBooleanExtra("returnToMovie", false)) {
                Intent intent = new Intent(this, SeatReservationActivity.class);
                intent.putExtra("movieId", getIntent().getIntExtra("movieId", -1));
                intent.putExtra("movieTitle", getIntent().getStringExtra("movieTitle"));
                intent.putExtra("moviePrice", getIntent().getDoubleExtra("moviePrice", 0.0));
                startActivity(intent);
            } else {
                startActivity(new Intent(this, MovieListActivity.class));
            }
            finish();
        } else {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
        }
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_forgot_password, null);
        builder.setView(dialogView);

        TextInputEditText usernameInput = dialogView.findViewById(R.id.usernameInput);
        TextInputEditText securityAnswerInput = dialogView.findViewById(R.id.securityAnswerInput);
        TextInputEditText newPasswordInput = dialogView.findViewById(R.id.newPasswordInput);

        AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.resetButton).setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String securityAnswer = securityAnswerInput.getText().toString().trim();
            String newPassword = newPasswordInput.getText().toString().trim();

            if (username.isEmpty() || securityAnswer.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verify security answer and update password
            if (dbHelper.verifySecurityAnswer(username, securityAnswer)) {
                if (dbHelper.updatePassword(username, newPassword)) {
                    Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Invalid username or security answer", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
} 