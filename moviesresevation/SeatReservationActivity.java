package com.example.moviesresevation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import android.app.AlertDialog;
import android.view.View;
import android.util.Log;

public class SeatReservationActivity extends AppCompatActivity {
    private GridLayout seatGrid;
    private TextView selectedSeatsText;
    private TextView totalPriceText;
    private Button confirmButton;
    private DatabaseHelper dbHelper;
    private int movieId;
    private double moviePrice;
    private String movieTitle;
    private List<String> selectedSeats;
    private List<String> reservedSeats;
    private List<String> userReservations;
    private String currentUsername;
    private static final int ROWS = 8;
    private static final int COLS = 6;
    private RadioGroup timeScheduleGroup;
    private String selectedTime = "11:00 AM"; // Default time

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_reservation);

        try {
            // Initialize database
            dbHelper = new DatabaseHelper(this);

            // Get intent extras
            movieId = getIntent().getIntExtra("movieId", -1);
            movieTitle = getIntent().getStringExtra("movieTitle");
            moviePrice = getIntent().getDoubleExtra("moviePrice", 0.0);

            // Log the received values
            Log.d("SeatReservation", "Received movieId: " + movieId);
            Log.d("SeatReservation", "Received movieTitle: " + movieTitle);
            Log.d("SeatReservation", "Received moviePrice: " + moviePrice);

            if (movieId == -1 || movieTitle == null || moviePrice == 0.0) {
                Toast.makeText(this, "Error loading movie details", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Get current username from SharedPreferences
            SharedPreferences prefs = getSharedPreferences("MovieApp", MODE_PRIVATE);
            currentUsername = prefs.getString("username", "");

            if (currentUsername.isEmpty()) {
                Intent loginIntent = new Intent(this, LoginActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                loginIntent.putExtra("returnToMovie", true);
                loginIntent.putExtra("movieId", movieId);
                loginIntent.putExtra("movieTitle", movieTitle);
                loginIntent.putExtra("moviePrice", moviePrice);
                startActivity(loginIntent);
                finish();
                return;
            }

            // Initialize views and continue with the rest of the setup
            initializeViews();
            setupTimeSchedule();
            selectedSeats = new ArrayList<>();
            refreshReservedSeats();

            setTitle(movieTitle + " - Select Seats");
            confirmButton.setOnClickListener(v -> confirmReservation());
            updateSelectionInfo();

        } catch (Exception e) {
            Log.e("SeatReservation", "Error in onCreate: " + e.getMessage());
            Toast.makeText(this, "Error initializing seat reservation", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        seatGrid = findViewById(R.id.seatGrid);
        selectedSeatsText = findViewById(R.id.selectedSeatsText);
        totalPriceText = findViewById(R.id.totalPriceText);
        confirmButton = findViewById(R.id.confirmButton);
        timeScheduleGroup = findViewById(R.id.timeScheduleGroup);
    }

    private void setupTimeSchedule() {
        timeScheduleGroup.setOnCheckedChangeListener((group, checkedId) -> {
            selectedSeats.clear();
            if (checkedId == R.id.time1) {
                selectedTime = "11:00 AM";
            } else if (checkedId == R.id.time2) {
                selectedTime = "2:30 PM";
            } else if (checkedId == R.id.time3) {
                selectedTime = "6:00 PM";
            } else if (checkedId == R.id.time4) {
                selectedTime = "9:30 PM";
            }
            refreshReservedSeats();
            updateSelectionInfo();
        });
    }

    private void refreshReservedSeats() {
        try {
            reservedSeats = dbHelper.getReservedSeats(movieId, selectedTime);
            userReservations = dbHelper.getUserReservations(currentUsername, movieId, selectedTime);
            createSeatGrid(); // Recreate the entire grid with updated reservation status

            // Clear selected seats when refreshing
            selectedSeats.clear();
            updateSelectionInfo();
        } catch (Exception e) {
            Log.e("SeatReservation", "Error refreshing seats: " + e.getMessage());
            Toast.makeText(this, "Error loading seats. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void createSeatGrid() {
        seatGrid.removeAllViews();
        seatGrid.setRowCount(ROWS);
        seatGrid.setColumnCount(COLS);

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                Button seat = new Button(this);
                String seatNumber = (char)('A' + i) + String.valueOf(j + 1);
                seat.setText(seatNumber);
                seat.setTag(seatNumber);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = getResources().getDimensionPixelSize(R.dimen.seat_width);
                params.height = getResources().getDimensionPixelSize(R.dimen.seat_height);
                params.setMargins(4, 4, 4, 4);
                params.rowSpec = GridLayout.spec(i);
                params.columnSpec = GridLayout.spec(j);
                seat.setLayoutParams(params);

                updateSeatState(seat, seatNumber);
                seat.setOnClickListener(v -> toggleSeatSelection((Button) v));

                seatGrid.addView(seat);
            }
        }
    }

    private void updateSeatState(Button seat, String seatNumber) {
        if (reservedSeats.contains(seatNumber)) {
            // Seat is already taken
            seat.setBackgroundColor(getResources().getColor(R.color.reserved_color));
            seat.setEnabled(false); // Disable only taken seats
            seat.setAlpha(0.5f); // Make it slightly transparent to show it's taken
        } else if (selectedSeats.contains(seatNumber)) {
            // Currently selected seat
            seat.setBackgroundColor(getResources().getColor(R.color.selected_seat));
            seat.setEnabled(true);
            seat.setAlpha(1.0f);
        } else {
            // Available seat
            seat.setBackgroundColor(getResources().getColor(R.color.available_color));
            seat.setEnabled(true);
            seat.setAlpha(1.0f);
        }
    }

    private void toggleSeatSelection(Button seat) {
        if (!seat.isEnabled()) {
            Toast.makeText(this, "This seat is already taken", Toast.LENGTH_SHORT).show();
            return;
        }

        String seatNumber = (String) seat.getTag();

        if (selectedSeats.contains(seatNumber)) {
            selectedSeats.remove(seatNumber);
            // Animation for deselection
            seat.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .withEndAction(() -> updateSeatState(seat, seatNumber));
        } else {
            if (selectedSeats.size() >= 4) {
                Toast.makeText(this, "Maximum 4 seats allowed", Toast.LENGTH_SHORT).show();
                return;
            }
            selectedSeats.add(seatNumber);
            // Animation for selection
            seat.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        seat.setBackgroundColor(getResources().getColor(R.color.selected_seat));
                        seat.setAlpha(1.0f);
                    });
        }

        updateSelectionInfo();
    }

    private void updateSelectionInfo() {
        if (selectedSeats.isEmpty()) {
            selectedSeatsText.setText("No seats selected");
            totalPriceText.setText("Total: ₱0.00");
            confirmButton.setEnabled(false);
        } else {
            selectedSeatsText.setText("Selected: " + String.join(", ", selectedSeats));
            double total = selectedSeats.size() * moviePrice;
            totalPriceText.setText(String.format("Total: ₱%.2f", total));
            confirmButton.setEnabled(true);
        }
    }

    private void confirmReservation() {
        if (selectedSeats.isEmpty()) {
            Toast.makeText(this, "Please select at least one seat", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedTime.isEmpty()) {
            Toast.makeText(this, "Please select a show time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show payment method dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Payment Method");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_payment_method, null);
        builder.setView(dialogView);

        RadioGroup paymentGroup = dialogView.findViewById(R.id.paymentMethodGroup);
        Button confirmPaymentButton = dialogView.findViewById(R.id.confirmPaymentButton);

        AlertDialog dialog = builder.create();
        dialog.show();

        confirmPaymentButton.setOnClickListener(v -> {
            int selectedId = paymentGroup.getCheckedRadioButtonId();
            String paymentMethod;

            if (selectedId == R.id.radioCash) {
                paymentMethod = "Cash";
                processReservation(paymentMethod);
                dialog.dismiss();
            } else if (selectedId == R.id.radioOnline) {
                paymentMethod = "Online";
                // Show online payment options
                showOnlinePaymentDialog();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showOnlinePaymentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Online Payment");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_online_payment, null);
        builder.setView(dialogView);

        RadioGroup onlinePaymentGroup = dialogView.findViewById(R.id.onlinePaymentGroup);
        Button confirmOnlineButton = dialogView.findViewById(R.id.confirmOnlineButton);

        AlertDialog dialog = builder.create();
        dialog.show();

        confirmOnlineButton.setOnClickListener(v -> {
            int selectedId = onlinePaymentGroup.getCheckedRadioButtonId();
            String paymentMethod;

            if (selectedId == R.id.radioGcash) {
                paymentMethod = "GCash";
            } else if (selectedId == R.id.radioPaymaya) {
                paymentMethod = "PayMaya";
            } else {
                Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
                return;
            }

            processReservation(paymentMethod);
            dialog.dismiss();
        });
    }

    private void processReservation(String paymentMethod) {
        // Disable all seats during processing
        setSeatsEnabled(false);

        try {
            // Determine payment status and details
            String paymentStatus = paymentMethod.equals("Cash") ? "Unpaid" : "Paid";
            String paymentDetails = paymentMethod.equals("Cash") ?
                    "Please pay at the counter" : "Paid via " + paymentMethod;
            double totalPrice = selectedSeats.size() * moviePrice;

            // Add new reservations with payment information
            boolean success = true;
            for (String seat : selectedSeats) {
                boolean reservationSuccess = dbHelper.addReservation(
                        currentUsername,
                        movieId,
                        seat,
                        selectedTime,
                        paymentMethod,
                        paymentStatus,
                        paymentDetails,
                        moviePrice  // price per seat
                );

                if (!reservationSuccess) {
                    success = false;
                    Log.e("SeatReservation", "Failed to reserve seat: " + seat);
                    break;
                }
            }

            if (success) {
                // Create intent for ReceiptActivity
                Intent intent = new Intent(this, ReceiptActivity.class);
                intent.putExtra("movieId", movieId);
                intent.putExtra("movieTitle", movieTitle);
                intent.putStringArrayListExtra("selectedSeats", new ArrayList<>(selectedSeats));
                intent.putExtra("showTime", selectedTime);
                intent.putExtra("totalPrice", totalPrice);
                intent.putExtra("username", currentUsername);
                intent.putExtra("paymentMethod", paymentMethod);
                intent.putExtra("paymentStatus", paymentStatus);
                intent.putExtra("paymentDetails", paymentDetails);

                // Show payment confirmation dialog before proceeding
                showPaymentConfirmationDialog(intent, paymentMethod, paymentStatus, totalPrice);
            } else {
                Toast.makeText(this, "Error making reservation. Please try again.", Toast.LENGTH_LONG).show();
                // Re-enable seats if reservation fails
                setSeatsEnabled(true);
            }
        } catch (Exception e) {
            Log.e("SeatReservation", "Error processing reservation: " + e.getMessage());
            Toast.makeText(this, "An error occurred. Please try again.", Toast.LENGTH_LONG).show();
            setSeatsEnabled(true);
        }
    }

    private void showPaymentConfirmationDialog(Intent receiptIntent, String paymentMethod, String paymentStatus, double totalPrice) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Payment Confirmation");

        // Create custom dialog view
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_payment_confirmation, null);
        builder.setView(dialogView);

        // Initialize dialog views
        TextView paymentMethodText = dialogView.findViewById(R.id.paymentMethodText);
        TextView paymentStatusText = dialogView.findViewById(R.id.paymentStatusText);
        TextView totalAmountText = dialogView.findViewById(R.id.totalAmountText);
        Button confirmButton = dialogView.findViewById(R.id.confirmButton);

        // Set dialog content
        paymentMethodText.setText("Payment Method: " + paymentMethod);
        paymentStatusText.setText("Status: " + paymentStatus);
        totalAmountText.setText(String.format("Total Amount: ₱%.2f", totalPrice));

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false); // Prevent dialog from being dismissed by back button

        confirmButton.setOnClickListener(v -> {
            if (paymentMethod.equals("Cash")) {
                // Show additional instructions for cash payment
                Toast.makeText(this, "Please proceed to counter for payment", Toast.LENGTH_LONG).show();
            } else {
                // Show success message for online payment
                Toast.makeText(this, "Payment successful via " + paymentMethod, Toast.LENGTH_LONG).show();
            }
            startActivity(receiptIntent);
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }

    // Helper method to enable/disable all seats
    private void setSeatsEnabled(boolean enabled) {
        for (int i = 0; i < seatGrid.getChildCount(); i++) {
            View child = seatGrid.getChildAt(i);
            if (child instanceof Button) {
                Button seat = (Button) child;
                String seatNumber = (String) seat.getTag();
                if (!reservedSeats.contains(seatNumber) && !userReservations.contains(seatNumber)) {
                    seat.setEnabled(enabled);
                }
            }
        }
        confirmButton.setEnabled(enabled);
    }
}