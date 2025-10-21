package com.gash;

public class User {
    private int id;
    private String name;
    private String email;
    private String number;
    private String pin;
    private double balance; // ✅ new field

    public User(int id, String name, String email, String number, String pin) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.number = number;
        this.pin = pin;
        this.balance = 0.0; // default
    }

    // ✅ Add this overloaded constructor (used when balance is known)
    public User(int id, String name, String email, String number, String pin, double balance) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.number = number;
        this.pin = pin;
        this.balance = balance;
    }

    // --- Getters and Setters ---
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getNumber() { return number; }
    public String getPin() { return pin; }
    public double getBalance() { return balance; }

    public void setPin(String pin) { this.pin = pin; }
    public void setBalance(double balance) { this.balance = balance; }

    @Override
    public String toString() {
        return name + " (" + email + ") - Balance: ₱" + balance;
    }
}
