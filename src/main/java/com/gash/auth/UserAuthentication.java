package com.gash.auth;

import com.gash.User;
import java.sql.*;
import java.util.Scanner;
import org.mindrot.jbcrypt.BCrypt;

public class UserAuthentication {
    private Connection conn;
    private User loggedInUser = null;
    private Scanner sc = new Scanner(System.in);

    public UserAuthentication() {
        try {
            conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/gash_db",
                    "root", // your MySQL username
                    "Putanginamo09_"      // your MySQL password (if any)
            );
            System.out.println("✅ Connected to MySQL database!");
        } catch (SQLException e) {
            System.out.println("❌ Database connection failed: " + e.getMessage());
        }
    }

    // === REGISTER USER ===
    public void registerUser() {
        System.out.println("\n--- Register New User ---");

        System.out.print("Enter Name: ");
        String name = sc.nextLine();

        System.out.print("Enter Email: ");
        String email = sc.nextLine();

        System.out.print("Enter Number: ");
        String number = sc.nextLine();

        System.out.print("Enter 4-digit PIN: ");
        String pin = sc.nextLine();

        if (pin.length() != 4 || !pin.matches("\\d+")) {
            System.out.println("PIN must be exactly 4 digits!");
            return;
        }

        String hashedPin = BCrypt.hashpw(pin, BCrypt.gensalt());

        try {
            // ✅ include balance with default 0.00
            String sql = "INSERT INTO users (name, email, number, pin, balance) VALUES (?, ?, ?, ?, 0.00)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, number);
            ps.setString(4, hashedPin);
            ps.executeUpdate();
            System.out.println("✅ Registration successful!");
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                System.out.println("❌ Email is already registered!");
            } else {
                System.out.println("❌ Error: " + e.getMessage());
            }
        }
    }

    // === LOGIN ===
    public void loginUser() {
        System.out.println("\n--- Login ---");

        System.out.print("Enter Email: ");
        String email = sc.nextLine();

        System.out.print("Enter PIN: ");
        String pin = sc.nextLine();

        try {
            String sql = "SELECT * FROM users WHERE email = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedPin = rs.getString("pin");
                if (BCrypt.checkpw(pin, storedPin)) {
                    // ✅ load balance as well
                    loggedInUser = new User(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("number"),
                            storedPin,
                            rs.getDouble("balance")
                    );
                    System.out.println("✅ Login successful! Welcome, " + loggedInUser.getName());
                    System.out.println("💰 Current Balance: ₱" + loggedInUser.getBalance());
                } else {
                    System.out.println("❌ Incorrect PIN!");
                }
            } else {
                System.out.println("❌ User not found!");
            }

        } catch (SQLException e) {
            System.out.println("❌ Login error: " + e.getMessage());
        }
    }

    // === CHANGE PIN ===
    public void changePin() {
        if (loggedInUser == null) {
            System.out.println("Please log in first!");
            return;
        }

        System.out.print("Enter current PIN: ");
        String currentPin = sc.nextLine();

        if (!BCrypt.checkpw(currentPin, loggedInUser.getPin())) {
            System.out.println("❌ Incorrect current PIN!");
            return;
        }

        System.out.print("Enter new 4-digit PIN: ");
        String newPin = sc.nextLine();

        if (newPin.length() != 4 || !newPin.matches("\\d+")) {
            System.out.println("Invalid new PIN!");
            return;
        }

        String hashedNewPin = BCrypt.hashpw(newPin, BCrypt.gensalt());

        try {
            String sql = "UPDATE users SET pin = ? WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, hashedNewPin);
            ps.setInt(2, loggedInUser.getId());
            ps.executeUpdate();

            loggedInUser.setPin(hashedNewPin);
            System.out.println("✅ PIN successfully changed!");

        } catch (SQLException e) {
            System.out.println("❌ Error changing PIN: " + e.getMessage());
        }
    }

    // === LOGOUT ===
    public void logout() {
        if (loggedInUser != null) {
            System.out.println("👋 Goodbye, " + loggedInUser.getName());
            loggedInUser = null;
        } else {
            System.out.println("No user is logged in.");
        }
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public Connection getConnection() {
        return conn;
    }
}
