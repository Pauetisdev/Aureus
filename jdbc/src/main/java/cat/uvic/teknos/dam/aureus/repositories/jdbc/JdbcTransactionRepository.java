package cat.uvic.teknos.dam.aureus.repositories.jdbc;

import cat.uvic.teknos.dam.aureus.Transaction;
import cat.uvic.teknos.dam.aureus.impl.TransactionImpl;
import cat.uvic.teknos.dam.aureus.repositories.TransactionRepository;
import cat.uvic.teknos.dam.aureus.repositories.UserRepository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.DataSource;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        // Verificar que existen los usuarios
        if (userRepository.get(transaction.getBuyer().getId()) == null ||
                userRepository.get(transaction.getSeller().getId()) == null) {
            throw new EntityNotFoundException("Buyer or Seller not found");
        }

        String sql;
        if (transaction.getId() == null) {
            sql = "INSERT INTO TRANSACTION (TRANSACTION_DATE, BUYER_ID, SELLER_ID) VALUES (?, ?, ?)";
        } else {
            sql = "UPDATE TRANSACTION SET TRANSACTION_DATE = ?, BUYER_ID = ?, SELLER_ID = ? WHERE TRANSACTION_ID = ?";
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

            // Obtener ID generado para inserciones
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

        String sql = "DELETE FROM TRANSACTION WHERE TRANSACTION_ID = ?";
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
        if (id == null) {
            throw new InvalidDataException("Transaction ID cannot be null");
        }

        String sql = "SELECT * FROM TRANSACTION WHERE TRANSACTION_ID = ?";
        try (var connection = dataSource.getConnection();
             var ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapTransaction(rs);
                } else {
                    throw new EntityNotFoundException("Transaction not found with ID " + id);
                }
            }

        } catch (SQLException e) {
            throw new RepositoryException("Error getting transaction", e);
        }
    }

    @Override
    public Set<Transaction> getAll() {
        Set<Transaction> transactions = new HashSet<>();
        String sql = "SELECT * FROM TRANSACTION";

        try (var connection = dataSource.getConnection();
             var ps = connection.prepareStatement(sql);
             var rs = ps.executeQuery()) {

            while (rs.next()) {
                transactions.add(mapTransaction(rs));
            }

        } catch (SQLException e) {
            throw new RepositoryException("Error getting all transactions", e);
        }

        return transactions;
    }

    @Override
    public List<Transaction> findByDateRange(Timestamp start, Timestamp end) {
        if (start == null || end == null) {
            throw new InvalidDataException("Start and End timestamps cannot be null");
        }

        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM TRANSACTION WHERE TRANSACTION_DATE BETWEEN ? AND ?";

        try (var connection = dataSource.getConnection();
             var ps = connection.prepareStatement(sql)) {

            ps.setTimestamp(1, start);
            ps.setTimestamp(2, end);

            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapTransaction(rs));
                }
            }

        } catch (SQLException e) {
            throw new RepositoryException("Error finding transactions by date range", e);
        }

        return transactions;
    }

    private Transaction mapTransaction(ResultSet rs) throws SQLException {
        var transaction = new TransactionImpl();
        transaction.setId(rs.getInt("TRANSACTION_ID"));
        transaction.setTransactionDate(rs.getTimestamp("TRANSACTION_DATE"));

        var buyer = userRepository.get(rs.getInt("BUYER_ID"));
        var seller = userRepository.get(rs.getInt("SELLER_ID"));

        if (buyer == null || seller == null) {
            throw new EntityNotFoundException("Buyer or Seller not found");
        }

        transaction.setBuyer(buyer);
        transaction.setSeller(seller);

        return transaction;
    }
}