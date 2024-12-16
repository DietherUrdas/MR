package com.example.moviesresevation;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryLogAdapter extends RecyclerView.Adapter<HistoryLogAdapter.ViewHolder> {
    private List<HistoryLog> historyLogs;
    private DatabaseHelper dbHelper;

    public HistoryLogAdapter(List<HistoryLog> historyLogs, DatabaseHelper dbHelper) {
        this.historyLogs = historyLogs;
        this.dbHelper = dbHelper;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        HistoryLog log = historyLogs.get(position);
        
        // Format the details text to look like a receipt
        String details = log.getDetails();
        String[] lines = details.split("\n");
        StringBuilder formattedDetails = new StringBuilder();
        
        // Add timestamp at the top
        formattedDetails.append("Date: ").append(log.getTimestamp()).append("\n");
        
        // Add the main details
        for (String line : lines) {
            formattedDetails.append(line).append("\n");
        }


        // Set the formatted text
        holder.detailsText.setText(formattedDetails.toString());

        // Hide the other TextViews since we're showing everything in detailsText
        holder.actionTypeText.setVisibility(View.GONE);
        holder.tableNameText.setVisibility(View.GONE);
        holder.recordIdText.setVisibility(View.GONE);
        holder.timestampText.setVisibility(View.GONE);

        holder.deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog(v.getContext(), position, log));
    }

    private void showDeleteConfirmationDialog(Context context, int position, HistoryLog log) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_delete_confirmation, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        dialog.show();

        dialogView.findViewById(R.id.yesButton).setOnClickListener(v -> {
            if (dbHelper.deleteHistoryLog(log.getId())) {
                historyLogs.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, historyLogs.size());
                Toast.makeText(context, "History log deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to delete history log", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.noButton).setOnClickListener(v -> dialog.dismiss());
    }

    @Override
    public int getItemCount() {
        return historyLogs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView actionTypeText, tableNameText, recordIdText, detailsText, timestampText;
        ImageButton deleteButton;

        ViewHolder(View itemView) {
            super(itemView);
            actionTypeText = itemView.findViewById(R.id.actionTypeText);
            tableNameText = itemView.findViewById(R.id.tableNameText);
            recordIdText = itemView.findViewById(R.id.recordIdText);
            detailsText = itemView.findViewById(R.id.detailsText);
            timestampText = itemView.findViewById(R.id.timestampText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
} 