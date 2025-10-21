package com.gash;

public class Transaction {
    private int id;
    private int userId;
    private String type;
    private double amount;
    private String date;

    public Transaction(int id, int userId, String type, double amount, String date) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.date = date;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getDate() { return date; }

    @Override
    public String toString() {
        return "[" + date + "] " + type + " ₱" + amount;
    }
}
