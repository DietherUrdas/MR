package com.example.moviesresevation;

public class HistoryLog {
    private int id;
    private String actionType;
    private String tableName;
    private String recordId;
    private String details;
    private String timestamp;

    public HistoryLog(int id, String actionType, String tableName, 
                     String recordId, String details, String timestamp) {
        this.id = id;
        this.actionType = actionType;
        this.tableName = tableName;
        this.recordId = recordId;
        this.details = details;
        this.timestamp = timestamp;
    }

    // Getters
    public int getId() { return id; }
    public String getActionType() { return actionType; }
    public String getTableName() { return tableName; }
    public String getRecordId() { return recordId; }
    public String getDetails() { return details; }
    public String getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "Action: " + actionType + 
               "\nTable: " + tableName + 
               "\nRecord: " + recordId + 
               "\nDetails: " + details + 
               "\nTime: " + timestamp;
    }
} 