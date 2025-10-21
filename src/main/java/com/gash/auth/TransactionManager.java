package com.gash.auth;

import com.gash.Transaction;
import com.gash.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TransactionManager {
    private Connection conn;
    private User loggedInUser;
    private Scanner sc = new Scanner(System.in);

    public TransactionManager(Connection conn, User user) {
        this.conn = conn;
        this.loggedInUser = user;
    }

    // === CASH IN ===
    public void cashIn() {
        System.out.print("Enter amount to cash in: ₱");
        double amount = sc.nextDouble();
        sc.nextLine();

        if (amount <= 0) {
            System.out.println("❌ Invalid amount!");
            return;
        }

        try {
            // Record transaction
            String sql = "INSERT INTO transactions (user_id, type, amount) VALUES (?, 'CASH_IN', ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, loggedInUser.getId());
            ps.setDouble(2, amount);
            ps.executeUpdate();

            // Update balance in database
            String updateSql = "UPDATE users SET balance = balance + ? WHERE id = ?";
            PreparedStatement updatePs = conn.prepareStatement(updateSql);
            updatePs.setDouble(1, amount);
            updatePs.setInt(2, loggedInUser.getId());
            updatePs.executeUpdate();

            // Update local balance
            loggedInUser.setBalance(loggedInUser.getBalance() + amount);

            System.out.println("✅ Successfully cashed in ₱" + amount);
            System.out.println("💰 New Balance: ₱" + loggedInUser.getBalance());
        } catch (SQLException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    // === CASH OUT ===
    public void cashOut() {
        System.out.print("Enter amount to cash out: ₱");
        double amount = sc.nextDouble();
        sc.nextLine();

        if (amount <= 0) {
            System.out.println("❌ Invalid amount!");
            return;
        }

        if (amount > loggedInUser.getBalance()) {
            System.out.println("❌ Insufficient funds!");
            return;
        }

        try {
            // Record transaction
            String sql = "INSERT INTO transactions (user_id, type, amount) VALUES (?, 'CASH_OUT', ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, loggedInUser.getId());
            ps.setDouble(2, amount);
            ps.executeUpdate();

            // Update balance in database
            String updateSql = "UPDATE users SET balance = balance - ? WHERE id = ?";
            PreparedStatement updatePs = conn.prepareStatement(updateSql);
            updatePs.setDouble(1, amount);
            updatePs.setInt(2, loggedInUser.getId());
            updatePs.executeUpdate();

            // Update local balance
            loggedInUser.setBalance(loggedInUser.getBalance() - amount);

            System.out.println("✅ Successfully cashed out ₱" + amount);
            System.out.println("💰 Remaining Balance: ₱" + loggedInUser.getBalance());
        } catch (SQLException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    // === VIEW TRANSACTIONS ===
    public void viewTransactions() {
        try {
            String sql = "SELECT * FROM transactions WHERE user_id = ? ORDER BY date DESC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, loggedInUser.getId());
            ResultSet rs = ps.executeQuery();

            List<Transaction> list = new ArrayList<>();
            while (rs.next()) {
                list.add(new Transaction(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("type"),
                        rs.getDouble("amount"),
                        rs.getString("date")
                ));
            }

            System.out.println("\n=== Your Transactions ===");
            if (list.isEmpty()) {
                System.out.println("No transactions found.");
            } else {
                list.forEach(System.out::println);
            }
        } catch (SQLException e) {
            System.out.println("❌ Error retrieving transactions: " + e.getMessage());
        }
    }
}
