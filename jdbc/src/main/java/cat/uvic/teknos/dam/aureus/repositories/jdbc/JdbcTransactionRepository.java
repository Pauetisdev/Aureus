package cat.uvic.teknos.dam.aureus.repositories.jdbc;

import cat.uvic.teknos.dam.aureus.Transaction;
import cat.uvic.teknos.dam.aureus.User;
import cat.uvic.teknos.dam.aureus.impl.TransactionImpl;
import cat.uvic.teknos.dam.aureus.impl.UserImpl;
import cat.uvic.teknos.dam.aureus.repositories.TransactionRepository;
import cat.uvic.teknos.dam.aureus.repositories.UserRepository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.DataSource;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcTransactionRepository implements TransactionRepository {
    private final DataSource dataSource;
    private final UserRepository userRepository;

    public JdbcTransactionRepository(DataSource dataSource, UserRepository userRepository) {
        if (dataSource == null || userRepository == null) {
            throw new InvalidDataException("DataSource and UserRepository cannot be null");
        }
        this.dataSource = dataSource;
        this.userRepository = userRepository;
    }

    @Override
    public void save(Transaction transaction) {
        if (transaction == null) {
            throw new InvalidDataException("Transaction cannot be null");
        }
        if (transaction.getBuyer() == null || transaction.getSeller() == null) {
            throw new InvalidDataException("Buyer and Seller must be set");
        }
        if (userRepository.get(transaction.getBuyer().getId()) == null ||
                userRepository.get(transaction.getSeller().getId()) == null) {
            throw new EntityNotFoundException("Buyer or Seller not found");
        }

        String sql;
        if (transaction.getId() == null) {
            sql = "INSERT INTO \"TRANSACTION\" (TRANSACTION_DATE, BUYER_ID, SELLER_ID) VALUES (?, ?, ?)";
        } else {
            sql = "UPDATE \"TRANSACTION\" SET TRANSACTION_DATE = ?, BUYER_ID = ?, SELLER_ID = ? WHERE TRANSACTION_ID = ?";
        }

        try (var connection = dataSource.getConnection();
             var ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setTimestamp(1, transaction.getTransactionDate());
            ps.setInt(2, transaction.getBuyer().getId());
            ps.setInt(3, transaction.getSeller().getId());

            if (transaction.getId() != null) {
                ps.setInt(4, transaction.getId());
            }

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new RepositoryException("Failed to save transaction");
            }

            if (transaction.getId() == null) {
                try (var rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        transaction.setId(rs.getInt(1));
                    }
                }
            }

        } catch (SQLException e) {
            throw new RepositoryException("Error saving transaction", e);
        }
    }

    @Override
    public void delete(Transaction transaction) {
        if (transaction == null || transaction.getId() == null) {
            throw new InvalidDataException("Transaction or Transaction ID cannot be null");
        }

        String sql = "DELETE FROM \"TRANSACTION\" WHERE TRANSACTION_ID = ?";
        try (var connection = dataSource.getConnection();
             var ps = connection.prepareStatement(sql)) {

            ps.setInt(1, transaction.getId());
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new EntityNotFoundException("Transaction not found with ID " + transaction.getId());
            }

        } catch (SQLException e) {
            throw new RepositoryException("Error deleting transaction", e);
        }
    }

    @Override
    public Transaction get(Integer id) {
        String sql = "SELECT * FROM \"TRANSACTION\" WHERE TRANSACTION_ID = ?";
        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int transactionId = rs.getInt("TRANSACTION_ID");
                    Timestamp transactionDate = rs.getTimestamp("TRANSACTION_DATE");
                    int buyerId = rs.getInt("BUYER_ID");
                    int sellerId = rs.getInt("SELLER_ID");
                    return buildTransaction(transactionId, transactionDate, buyerId, sellerId);
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Error getting transaction", e);
        }
        return null;
    }

    @Override
    public List<Transaction> getAll() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.*, " +
                "b.USER_ID as BUYER_ID, b.USERNAME as BUYER_USERNAME, b.EMAIL as BUYER_EMAIL, " +
                "s.USER_ID as SELLER_ID, s.USERNAME as SELLER_USERNAME, s.EMAIL as SELLER_EMAIL " +
                "FROM \"TRANSACTION\" t " +
                "JOIN \"USER\" b ON t.BUYER_ID = b.USER_ID " +
                "JOIN \"USER\" s ON t.SELLER_ID = s.USER_ID";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement st = conn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {

            while (rs.next()) {
                int transactionId = rs.getInt("TRANSACTION_ID");
                Timestamp transactionDate = rs.getTimestamp("TRANSACTION_DATE");

                // Crear buyer desde el ResultSet
                var buyer = new UserImpl(); // Asume que tienes UserImpl
                buyer.setId(rs.getInt("BUYER_ID"));
                buyer.setUsername(rs.getString("BUYER_USERNAME"));
                buyer.setEmail(rs.getString("BUYER_EMAIL"));

                // Crear seller desde el ResultSet
                var seller = new UserImpl();
                seller.setId(rs.getInt("SELLER_ID"));
                seller.setUsername(rs.getString("SELLER_USERNAME"));
                seller.setEmail(rs.getString("SELLER_EMAIL"));

                // Crear transaction
                var transaction = new TransactionImpl();
                transaction.setId(transactionId);
                transaction.setTransactionDate(transactionDate);
                transaction.setBuyer(buyer);
                transaction.setSeller(seller);

                transactions.add(transaction);
            }

        } catch (SQLException e) {
            throw new RepositoryException("Error getting all transactions", e);
        }

        return transactions;
    }

    public List<Transaction> findByDateRange(Timestamp start, Timestamp end) {
        if (start == null || end == null) {
            throw new InvalidDataException("Start and End timestamps cannot be null");
        }

        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM \"TRANSACTION\" WHERE TRANSACTION_DATE BETWEEN ? AND ?";

        try (var connection = dataSource.getConnection();
             var ps = connection.prepareStatement(sql)) {

            ps.setTimestamp(1, start);
            ps.setTimestamp(2, end);

            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    int transactionId = rs.getInt("TRANSACTION_ID");
                    Timestamp transactionDate = rs.getTimestamp("TRANSACTION_DATE");
                    int buyerId = rs.getInt("BUYER_ID");
                    int sellerId = rs.getInt("SELLER_ID");
                    transactions.add(buildTransaction(transactionId, transactionDate, buyerId, sellerId));
                }
            }

        } catch (SQLException e) {
            throw new RepositoryException("Error finding transactions by date range", e);
        }

        return transactions;
    }

    private Transaction buildTransaction(int id, Timestamp date, int buyerId, int sellerId) {
        var buyer = userRepository.get(buyerId);
        var seller = userRepository.get(sellerId);

        if (buyer == null) {
            throw new EntityNotFoundException("Buyer not found for transaction " + id);
        }
        if (seller == null) {
            throw new EntityNotFoundException("Seller not found for transaction " + id);
        }

        var transaction = new TransactionImpl();
        transaction.setId(id);
        transaction.setTransactionDate(date);
        transaction.setBuyer(buyer);
        transaction.setSeller(seller);

        return transaction;
    }
}
