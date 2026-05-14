package service;

import dao.AccountDAO;
import dao.TransactionDAO;
import dao.UserDAO;
import db.DBConnection;
import model.Account;
import model.Transaction;
import model.User;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class BankingService {
    private final Scanner scanner;
    private final UserDAO userDAO;
    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;
    private User loggedInUser;

    public BankingService() {
        scanner = new Scanner(System.in);
        userDAO = new UserDAO();
        accountDAO = new AccountDAO();
        transactionDAO = new TransactionDAO();
    }

    public void start() {
        boolean running = true;

        while (running) {
            showWelcomeMenu();
            int choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1:
                    register();
                    break;
                case 2:
                    login();
                    break;
                case 3:
                    running = false;
                    System.out.println("Thank you for using Banking Management System.");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        scanner.close();
    }

    private void showWelcomeMenu() {
        System.out.println();
        System.out.println("=================================");
        System.out.println("   BANKING MANAGEMENT SYSTEM");
        System.out.println("=================================");
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3. Exit");
    }

    private void register() {
        System.out.println("\n--- User Registration ---");
        String fullName = readText("Full name: ");
        String email = readText("Email: ");
        String phone = readText("Phone number: ");
        String password = readPassword("Password: ");

        if (fullName.length() < 3) {
            System.out.println("Name should contain at least 3 characters.");
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            System.out.println("Please enter a valid email address.");
            return;
        }
        if (password.length() < 4) {
            System.out.println("Password should contain at least 4 characters.");
            return;
        }
        if (userDAO.emailExists(email)) {
            System.out.println("This email is already registered.");
            return;
        }

        User user = new User(0, fullName, email, phone, password);
        if (userDAO.registerUser(user)) {
            System.out.println("Registration successful. You can login now.");
        } else {
            System.out.println("Registration failed. Please try again.");
        }
    }

    private void login() {
        System.out.println("\n--- User Login ---");
        String email = readText("Email: ");
        String password = readPassword("Password: ");

        loggedInUser = userDAO.loginUser(email, password);
        if (loggedInUser == null) {
            System.out.println("Invalid email or password.");
            return;
        }

        System.out.println("Welcome, " + loggedInUser.getFullName() + "!");
        showDashboard();
    }

    private void showDashboard() {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println();
            System.out.println("---------- Banking Dashboard ----------");
            System.out.println("1. Create Bank Account");
            System.out.println("2. Deposit Money");
            System.out.println("3. Withdraw Money");
            System.out.println("4. Balance Enquiry");
            System.out.println("5. Fund Transfer");
            System.out.println("6. View Transaction History");
            System.out.println("7. Change Password");
            System.out.println("8. Logout");
            System.out.println("9. Exit System");

            int choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1:
                    createAccount();
                    break;
                case 2:
                    depositMoney();
                    break;
                case 3:
                    withdrawMoney();
                    break;
                case 4:
                    balanceEnquiry();
                    break;
                case 5:
                    transferMoney();
                    break;
                case 6:
                    viewTransactionHistory();
                    break;
                case 7:
                    changePassword();
                    break;
                case 8:
                    loggedIn = false;
                    loggedInUser = null;
                    System.out.println("Logged out successfully.");
                    break;
                case 9:
                    System.out.println("Thank you for using Banking Management System.");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void createAccount() {
        if (getCurrentUserAccount() != null) {
            System.out.println("You already have an active bank account.");
            return;
        }

        System.out.println("\n--- Create Bank Account ---");
        System.out.println("1. Savings");
        System.out.println("2. Current");
        int typeChoice = readInt("Choose account type: ");

        String accountType;
        if (typeChoice == 1) {
            accountType = "SAVINGS";
        } else if (typeChoice == 2) {
            accountType = "CURRENT";
        } else {
            System.out.println("Invalid account type.");
            return;
        }

        BigDecimal openingBalance = readAmount("Opening balance: ");
        if (openingBalance.compareTo(BigDecimal.ZERO) < 0) {
            System.out.println("Opening balance cannot be negative.");
            return;
        }

        String accountNumber = generateAccountNumber();
        Account account = new Account(0, loggedInUser.getUserId(), accountNumber, accountType,
                openingBalance, "ACTIVE");

        if (accountDAO.createAccount(account)) {
            System.out.println("Account created successfully.");
            System.out.println("Your account number is: " + accountNumber);
        } else {
            System.out.println("Could not create account.");
        }
    }

    private void depositMoney() {
        Account account = requireAccount();
        if (account == null) {
            return;
        }

        BigDecimal amount = readAmount("Enter deposit amount: ");
        if (!isPositive(amount)) {
            System.out.println("Deposit amount must be greater than zero.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            Account lockedAccount = accountDAO.getAccountByNumber(con, account.getAccountNumber());
            BigDecimal newBalance = lockedAccount.getBalance().add(amount);

            accountDAO.updateBalance(con, lockedAccount.getAccountId(), newBalance);
            transactionDAO.addTransaction(con, new Transaction(0, lockedAccount.getAccountId(), "DEPOSIT",
                    amount, "Cash deposit", null, null));
            con.commit();
            System.out.println("Amount deposited successfully. New balance: " + newBalance);
        } catch (SQLException e) {
            System.out.println("Deposit failed: " + e.getMessage());
        }
    }

    private void withdrawMoney() {
        Account account = requireAccount();
        if (account == null) {
            return;
        }

        BigDecimal amount = readAmount("Enter withdrawal amount: ");
        if (!isPositive(amount)) {
            System.out.println("Withdrawal amount must be greater than zero.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            Account lockedAccount = accountDAO.getAccountByNumber(con, account.getAccountNumber());

            if (lockedAccount.getBalance().compareTo(amount) < 0) {
                con.rollback();
                System.out.println("Insufficient balance.");
                return;
            }

            BigDecimal newBalance = lockedAccount.getBalance().subtract(amount);
            accountDAO.updateBalance(con, lockedAccount.getAccountId(), newBalance);
            transactionDAO.addTransaction(con, new Transaction(0, lockedAccount.getAccountId(), "WITHDRAW",
                    amount, "Cash withdrawal", null, null));
            con.commit();
            System.out.println("Amount withdrawn successfully. New balance: " + newBalance);
        } catch (SQLException e) {
            System.out.println("Withdrawal failed: " + e.getMessage());
        }
    }

    private void balanceEnquiry() {
        Account account = requireAccount();
        if (account == null) {
            return;
        }

        System.out.println("\n--- Account Details ---");
        System.out.println("Account Number : " + account.getAccountNumber());
        System.out.println("Account Type   : " + account.getAccountType());
        System.out.println("Balance        : " + account.getBalance());
        System.out.println("Status         : " + account.getStatus());
    }

    private void transferMoney() {
        Account sender = requireAccount();
        if (sender == null) {
            return;
        }

        String receiverNumber = readText("Enter receiver account number: ");
        if (sender.getAccountNumber().equals(receiverNumber)) {
            System.out.println("You cannot transfer money to the same account.");
            return;
        }

        BigDecimal amount = readAmount("Enter transfer amount: ");
        if (!isPositive(amount)) {
            System.out.println("Transfer amount must be greater than zero.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            Account lockedSender = accountDAO.getAccountByNumber(con, sender.getAccountNumber());
            Account receiver = accountDAO.getAccountByNumber(con, receiverNumber);

            if (receiver == null) {
                con.rollback();
                System.out.println("Receiver account not found.");
                return;
            }
            if (lockedSender.getBalance().compareTo(amount) < 0) {
                con.rollback();
                System.out.println("Insufficient balance.");
                return;
            }

            BigDecimal senderBalance = lockedSender.getBalance().subtract(amount);
            BigDecimal receiverBalance = receiver.getBalance().add(amount);

            accountDAO.updateBalance(con, lockedSender.getAccountId(), senderBalance);
            accountDAO.updateBalance(con, receiver.getAccountId(), receiverBalance);

            transactionDAO.addTransaction(con, new Transaction(0, lockedSender.getAccountId(), "TRANSFER_OUT",
                    amount, "Transfer to " + receiver.getAccountNumber(), null, receiver.getAccountId()));
            transactionDAO.addTransaction(con, new Transaction(0, receiver.getAccountId(), "TRANSFER_IN",
                    amount, "Transfer from " + lockedSender.getAccountNumber(), null, lockedSender.getAccountId()));

            con.commit();
            System.out.println("Transfer successful. New balance: " + senderBalance);
        } catch (SQLException e) {
            System.out.println("Transfer failed: " + e.getMessage());
        }
    }

    private void viewTransactionHistory() {
        Account account = requireAccount();
        if (account == null) {
            return;
        }

        List<Transaction> transactions = transactionDAO.getTransactionsByAccountId(account.getAccountId());
        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }

        System.out.println("\n--- Transaction History ---");
        System.out.printf("%-5s %-20s %-15s %-12s %-30s%n",
                "ID", "Date", "Type", "Amount", "Description");
        for (Transaction t : transactions) {
            System.out.printf("%-5d %-20s %-15s %-12s %-30s%n",
                    t.getTransactionId(),
                    t.getTransactionDate(),
                    t.getTransactionType(),
                    t.getAmount(),
                    t.getDescription());
        }
    }

    private void changePassword() {
        String oldPassword = readPassword("Old password: ");
        String newPassword = readPassword("New password: ");

        if (newPassword.length() < 4) {
            System.out.println("New password should contain at least 4 characters.");
            return;
        }

        if (userDAO.changePassword(loggedInUser.getUserId(), oldPassword, newPassword)) {
            System.out.println("Password changed successfully.");
        } else {
            System.out.println("Old password is incorrect.");
        }
    }

    private Account requireAccount() {
        Account account = getCurrentUserAccount();
        if (account == null) {
            System.out.println("Please create a bank account first.");
        }
        return account;
    }

    private Account getCurrentUserAccount() {
        return accountDAO.getAccountByUserId(loggedInUser.getUserId());
    }

    private String generateAccountNumber() {
        Random random = new Random();
        String accountNumber;
        do {
            accountNumber = "10" + (10000000 + random.nextInt(90000000));
        } while (accountDAO.accountNumberExists(accountNumber));
        return accountNumber;
    }

    private boolean isPositive(BigDecimal amount) {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    private String readText(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }

    private String readPassword(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }

    private int readInt(String message) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private BigDecimal readAmount(String message) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();
            try {
                BigDecimal amount = new BigDecimal(input);
                return amount.setScale(2, java.math.RoundingMode.HALF_UP);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid amount.");
            }
        }
    }
}
