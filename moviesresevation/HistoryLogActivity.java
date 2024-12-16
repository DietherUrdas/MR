package com.example.moviesresevation;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryLogActivity extends AppCompatActivity {
    private RecyclerView historyRecyclerView;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_log);

        dbHelper = new DatabaseHelper(this);
        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<HistoryLog> historyLogs = dbHelper.getDeleteHistory();
        Log.d("HistoryLogActivity", "Number of history logs: " + historyLogs.size());

        if (historyLogs.isEmpty()) {
            Toast.makeText(this, "No activity history found", Toast.LENGTH_SHORT).show();
        } else {
            HistoryLogAdapter adapter = new HistoryLogAdapter(historyLogs, dbHelper);
            historyRecyclerView.setAdapter(adapter);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Activity History");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
} 