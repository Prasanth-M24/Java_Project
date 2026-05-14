package dao;

import db.DBConnection;
import model.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    public boolean addTransaction(Connection con, Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (account_id, transaction_type, amount, description, target_account_id) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, transaction.getAccountId());
            ps.setString(2, transaction.getTransactionType());
            ps.setBigDecimal(3, transaction.getAmount());
            ps.setString(4, transaction.getDescription());

            if (transaction.getTargetAccountId() == null) {
                ps.setNull(5, java.sql.Types.INTEGER);
            } else {
                ps.setInt(5, transaction.getTargetAccountId());
            }
            return ps.executeUpdate() > 0;
        }
    }

    public List<Transaction> getTransactionsByAccountId(int accountId) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_id = ? "
                + "ORDER BY transaction_date DESC, transaction_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transactions.add(extractTransaction(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Could not load transaction history: " + e.getMessage());
        }
        return transactions;
    }

    private Transaction extractTransaction(ResultSet rs) throws SQLException {
        int targetAccountId = rs.getInt("target_account_id");
        Integer target = rs.wasNull() ? null : targetAccountId;

        return new Transaction(
                rs.getInt("transaction_id"),
                rs.getInt("account_id"),
                rs.getString("transaction_type"),
                rs.getBigDecimal("amount"),
                rs.getString("description"),
                rs.getTimestamp("transaction_date"),
                target
        );
    }
}
