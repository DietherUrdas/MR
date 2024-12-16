package com.example.moviesresevation;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import android.graphics.pdf.PdfDocument;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.net.Uri;
import androidx.core.content.FileProvider;
import android.content.ActivityNotFoundException;
import android.util.Log;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.NonNull;
import android.os.Environment;
import android.provider.Settings;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.app.AlertDialog;
import android.view.View;

public class ReceiptActivity extends AppCompatActivity {
    private TextView movieTitleTextView;
    private TextView showTimeTextView;
    private TextView selectedSeatsTextView;
    private TextView totalPriceTextView;
    private TextView usernameTextView;
    private TextView paymentMethodTextView;
    private TextView paymentStatusTextView;
    private TextView paymentDetailsTextView;
    private Button bookMoreButton;

    private static final int PERMISSION_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        initializeViews();

        // Get data from intent
        String movieTitle = getIntent().getStringExtra("movieTitle");
        String showTime = getIntent().getStringExtra("showTime");
        ArrayList<String> selectedSeats = getIntent().getStringArrayListExtra("selectedSeats");
        double totalPrice = getIntent().getDoubleExtra("totalPrice", 0.0);
        String username = getIntent().getStringExtra("username");
        String paymentMethod = getIntent().getStringExtra("paymentMethod");
        String paymentStatus = getIntent().getStringExtra("paymentStatus");
        String paymentDetails = getIntent().getStringExtra("paymentDetails");

        setReceiptData(movieTitle, showTime, selectedSeats, totalPrice, 
                      username, paymentMethod, paymentStatus, paymentDetails);

        // Set up button click listener
        bookMoreButton.setOnClickListener(v -> {
            if (checkAndRequestPermissions()) {
                generatePDF(movieTitle, showTime, selectedSeats, totalPrice, 
                           username, paymentMethod, paymentStatus, paymentDetails);
            }
        });
    }

    private boolean checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                    return false;
                } catch (Exception e) {
                    Log.e("Permission", "Error requesting permission: " + e.getMessage());
                    return false;
                }
            }
            return true;
        } else {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE
                );
                return false;
            }
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, retry generating PDF
                String movieTitle = getIntent().getStringExtra("movieTitle");
                String showTime = getIntent().getStringExtra("showTime");
                ArrayList<String> selectedSeats = getIntent().getStringArrayListExtra("selectedSeats");
                double totalPrice = getIntent().getDoubleExtra("totalPrice", 0.0);
                String username = getIntent().getStringExtra("username");
                String paymentMethod = getIntent().getStringExtra("paymentMethod");
                String paymentStatus = getIntent().getStringExtra("paymentStatus");
                String paymentDetails = getIntent().getStringExtra("paymentDetails");
                
                generatePDF(movieTitle, showTime, selectedSeats, totalPrice, 
                           username, paymentMethod, paymentStatus, paymentDetails);
            } else {
                Toast.makeText(this, "Permission denied. Cannot generate PDF.", 
                              Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void generatePDF(String movieTitle, String showTime, 
                           ArrayList<String> selectedSeats, double totalPrice,
                           String username, String paymentMethod, 
                           String paymentStatus, String paymentDetails) {
        try {
            // Get the Downloads directory
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            Log.d("PDF", "Downloads dir: " + downloadsDir.getAbsolutePath());

            // Create MovieReservation folder inside Downloads
            File movieReservationDir = new File(downloadsDir, "MovieReservation");
            if (!movieReservationDir.exists()) {
                boolean created = movieReservationDir.mkdirs();
                Log.d("PDF", "Directory created: " + created);
            }

            // Create filename
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "Ticket_" + timeStamp + ".pdf";
            File pdfFile = new File(movieReservationDir, fileName);

            Log.d("PDF", "PDF path: " + pdfFile.getAbsolutePath());

            // Create PDF document
            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);

            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);

            // Draw border
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            canvas.drawRect(20, 20, pageInfo.getPageWidth() - 20, pageInfo.getPageHeight() - 20, paint);

            // Add header
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(24);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Movie Ticket Receipt", pageInfo.getPageWidth() / 2, 60, paint);

            // Add line separator
            paint.setStrokeWidth(1);
            canvas.drawLine(40, 80, pageInfo.getPageWidth() - 40, 80, paint);

            // Reset text size for details
            paint.setTextSize(14);
            paint.setTextAlign(Paint.Align.LEFT);
            int y = 100;
            int x = 50;

            // Add receipt details
            canvas.drawText("Date: " + timeStamp, x, y, paint);
            y += 20;
            canvas.drawText("Customer: " + username, x, y, paint);
            y += 20;
            canvas.drawText("Movie: " + movieTitle, x, y, paint);
            y += 20;
            canvas.drawText("Show Time: " + showTime, x, y, paint);
            y += 20;
            canvas.drawText("Seats: " + String.join(", ", selectedSeats), x, y, paint);
            y += 20;
            canvas.drawText("Payment Method: " + paymentMethod, x, y, paint);
            y += 20;
            canvas.drawText("Payment Status: " + paymentStatus, x, y, paint);
            y += 20;
            canvas.drawText(String.format("Total Amount: ₱%.2f", totalPrice), x, y, paint);

            // Add footer text
            y += 40;
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Thank you for your reservation!", pageInfo.getPageWidth() / 2, y, paint);

            // Add app logo
            try {
                Bitmap logoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.app_logo);
                if (logoBitmap != null) {
                    // Scale logo to appropriate size
                    float logoWidth = 300; // Adjust size as needed
                    float scale = logoWidth / logoBitmap.getWidth();
                    float logoHeight = logoBitmap.getHeight() * scale;

                    // Center the logo horizontally and position it near the bottom
                    float logoX = (pageInfo.getPageWidth() - logoWidth) / 2;
                    float logoY = pageInfo.getPageHeight() - 500; // Adjust position as needed

                    // Create scaled bitmap
                    Bitmap scaledLogo = Bitmap.createScaledBitmap(
                        logoBitmap, 
                        (int)logoWidth, 
                        (int)logoHeight, 
                        true
                    );

                    // Draw the logo
                    canvas.drawBitmap(scaledLogo, logoX, logoY, null);

                    // Add company name or website below logo
                    paint.setTextSize(10);
                    paint.setTextAlign(Paint.Align.CENTER);
                    canvas.drawText("w w w . m o v i e r e s e r v a t i o n . c o m",
                                  pageInfo.getPageWidth() / 2, 
                                  logoY + logoHeight + 15, paint);

                    // Recycle bitmaps
                    if (scaledLogo != logoBitmap) {
                        scaledLogo.recycle();
                    }
                    logoBitmap.recycle();
                }
            } catch (Exception e) {
                Log.e("PDF", "Error adding logo: " + e.getMessage());
            }

            // Finalize the page
            document.finishPage(page);

            // Write the document content
            FileOutputStream fos = new FileOutputStream(pdfFile);
            document.writeTo(fos);
            document.close();
            fos.close();

            // Open the PDF
            openPDF(pdfFile);

            // Note: Remove the MovieListActivity navigation from here
            // as it's now handled in the dialog's OK button

        } catch (IOException e) {
            Log.e("PDF", "Error generating PDF: " + e.getMessage());
            e.printStackTrace();
            // Show error in a dialog instead of Toast
            new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Failed to generate PDF: " + e.getMessage())
                .setPositiveButton("OK", null)
                .show();
        }
    }

    private void openPDF(File file) {
        Uri uri = FileProvider.getUriForFile(this, 
                getApplicationContext().getPackageName() + ".provider", file);
        
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Please install a PDF reader", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeViews() {
        movieTitleTextView = findViewById(R.id.movieTitleTextView);
        showTimeTextView = findViewById(R.id.showTimeTextView);
        selectedSeatsTextView = findViewById(R.id.selectedSeatsTextView);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        usernameTextView = findViewById(R.id.usernameTextView);
        paymentMethodTextView = findViewById(R.id.paymentMethodTextView);
        paymentStatusTextView = findViewById(R.id.paymentStatusTextView);
        paymentDetailsTextView = findViewById(R.id.paymentDetailsTextView);
        bookMoreButton = findViewById(R.id.bookMoreButton);
    }

    private void setReceiptData(String movieTitle, String showTime, 
                              ArrayList<String> selectedSeats, double totalPrice,
                              String username, String paymentMethod, 
                              String paymentStatus, String paymentDetails) {
        setTitle("Receipt");
        movieTitleTextView.setText("Movie: " + movieTitle);
        showTimeTextView.setText("Show Time: " + showTime);
        selectedSeatsTextView.setText("Seats: " + String.join(", ", selectedSeats));
        totalPriceTextView.setText(String.format("Total Price: ₱%.2f", totalPrice));
        usernameTextView.setText("Customer: " + username);
        paymentMethodTextView.setText("Payment Method: " + paymentMethod);
        paymentStatusTextView.setText("Status: " + paymentStatus);
        paymentDetailsTextView.setText(paymentDetails);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_print_confirmation, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Set up button clicks
        dialogView.findViewById(R.id.btnYes).setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(ReceiptActivity.this, MovieListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        dialogView.findViewById(R.id.btnNo).setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }
}