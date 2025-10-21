package com.gash.auth;

import com.gash.User;
import java.sql.*;
import java.util.Scanner;

public class CashTransfer {
    private Connection conn;
    private User loggedInUser;
    private Scanner sc = new Scanner(System.in);

    public CashTransfer(Connection conn, User user) {
        this.conn = conn;
        this.loggedInUser = user;
    }

    public void transferMoney() {
        System.out.print("Enter recipient’s email or mobile number: ");
        String recipient = sc.nextLine();

        System.out.print("Enter amount to transfer: ₱");
        double amount = sc.nextDouble();
        sc.nextLine();

        if (amount <= 0) {
            System.out.println("❌ Invalid amount!");
            return;
        }

        if (amount > loggedInUser.getBalance()) {
            System.out.println("❌ Insufficient balance!");
            return;
        }

        try {
            // Find recipient
            String findSql = "SELECT id, name FROM users WHERE email = ? OR number = ?";
            PreparedStatement findPs = conn.prepareStatement(findSql);
            findPs.setString(1, recipient);
            findPs.setString(2, recipient);
            ResultSet rs = findPs.executeQuery();

            if (!rs.next()) {
                System.out.println("❌ Recipient not found!");
                return;
            }

            int recipientId = rs.getInt("id");
            String recipientName = rs.getString("name");

            if (recipientId == loggedInUser.getId()) {
                System.out.println("❌ You cannot transfer to yourself!");
                return;
            }

            conn.setAutoCommit(false); // begin transaction

            // Deduct from sender
            String deductSql = "UPDATE users SET balance = balance - ? WHERE id = ?";
            PreparedStatement deductPs = conn.prepareStatement(deductSql);
            deductPs.setDouble(1, amount);
            deductPs.setInt(2, loggedInUser.getId());
            deductPs.executeUpdate();

            // Add to receiver
            String addSql = "UPDATE users SET balance = balance + ? WHERE id = ?";
            PreparedStatement addPs = conn.prepareStatement(addSql);
            addPs.setDouble(1, amount);
            addPs.setInt(2, recipientId);
            addPs.executeUpdate();

            // Record transactions
            String senderTx = "INSERT INTO transactions (user_id, type, amount, target_user_id) VALUES (?, 'TRANSFER_OUT', ?, ?)";
            PreparedStatement senderPs = conn.prepareStatement(senderTx);
            senderPs.setInt(1, loggedInUser.getId());
            senderPs.setDouble(2, amount);
            senderPs.setInt(3, recipientId);
            senderPs.executeUpdate();

            String receiverTx = "INSERT INTO transactions (user_id, type, amount, target_user_id) VALUES (?, 'TRANSFER_IN', ?, ?)";
            PreparedStatement receiverPs = conn.prepareStatement(receiverTx);
            receiverPs.setInt(1, recipientId);
            receiverPs.setDouble(2, amount);
            receiverPs.setInt(3, loggedInUser.getId());
            receiverPs.executeUpdate();

            conn.commit();
            loggedInUser.setBalance(loggedInUser.getBalance() - amount);

            System.out.println("✅ Successfully transferred ₱" + amount + " to " + recipientName + " (" + recipient + ")");
            System.out.println("💰 Remaining Balance: ₱" + loggedInUser.getBalance());

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                System.out.println("⚠️ Rollback failed: " + rollbackEx.getMessage());
            }
            System.out.println("❌ Error transferring funds: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("⚠️ AutoCommit restore failed: " + e.getMessage());
            }
        }
    }
}
