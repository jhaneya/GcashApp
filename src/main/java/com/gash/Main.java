package com.gash;

import java.util.Scanner;
import com.gash.auth.UserAuthentication;
import com.gash.auth.TransactionManager;
import com.gash.auth.CashTransfer;

public class Main {
    public static void main(String[] args) {
        UserAuthentication auth = new UserAuthentication();
        TransactionManager tm = null;
        Scanner sc = new Scanner(System.in);
        int choice;

        do {
            System.out.println("\n===== Gash Online Banking =====");
            if (auth.getLoggedInUser() != null) {
                System.out.println("👤 Logged in as: " + auth.getLoggedInUser().getName());
                System.out.println("💰 Balance: ₱" + auth.getLoggedInUser().getBalance());
            }
            System.out.println("--------------------------------");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Change PIN");
            System.out.println("4. Logout");
            System.out.println("5. Cash In");
            System.out.println("6. Cash Out");
            System.out.println("7. View Transactions");
            System.out.println("8. Check Balance");
            System.out.println("9. Transfer Money");
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");
            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    auth.registerUser();
                    break;

                case 2:
                    auth.loginUser();
                    if (auth.getLoggedInUser() != null) {
                        tm = new TransactionManager(auth.getConnection(), auth.getLoggedInUser());
                    }
                    break;

                case 3:
                    auth.changePin();
                    break;

                case 4:
                    auth.logout();
                    tm = null;
                    break;

                case 5:
                case 6:
                case 7:
                    if (auth.getLoggedInUser() == null || tm == null) {
                        System.out.println("Please log in first!");
                        break;
                    }
                    if (choice == 5) tm.cashIn();
                    else if (choice == 6) tm.cashOut();
                    else tm.viewTransactions();
                    break;

                case 8:
                    if (auth.getLoggedInUser() == null) {
                        System.out.println("Please log in first!");
                        break;
                    }
                    System.out.println("💰 Current Balance: ₱" + auth.getLoggedInUser().getBalance());
                    break;
                case 9:
                    if (auth.getLoggedInUser() == null) {
                        System.out.println("Please log in first!");
                        break;
                    }
                    CashTransfer transfer = new CashTransfer(auth.getConnection(), auth.getLoggedInUser());
                    transfer.transferMoney();
                    break;
                case 0:
                    System.out.println("Exiting...");
                    break;

                default:
                    System.out.println("Invalid choice!");
            }
        } while (choice != 0);

        sc.close();
    }
}
