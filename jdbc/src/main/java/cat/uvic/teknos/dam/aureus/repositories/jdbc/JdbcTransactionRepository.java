package cat.uvic.teknos.dam.aureus.repositories.jdbc;

import cat.uvic.teknos.dam.aureus.Transaction;
import cat.uvic.teknos.dam.aureus.impl.TransactionImpl;
import cat.uvic.teknos.dam.aureus.repositories.TransactionRepository;
import cat.uvic.teknos.dam.aureus.repositories.UserRepository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.DataSource;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JdbcTransactionRepository implements TransactionRepository {
    private final DataSource dataSource;
    private UserRepository userRepository = null;


    public JdbcTransactionRepository(DataSource dataSource, UserRepository userRepository) {
        if (dataSource == null || userRepository == null) {
            throw new InvalidDataException("DataSource and UserRepository cannot be null");
        }
        this.dataSource = dataSource;
        this.userRepository = userRepository;
    }

    public JdbcTransactionRepository(DataSource dataSource) {
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
        if (userRepository.get(transaction.getBuyer().getId()) == null) {
            throw new EntityNotFoundException("Buyer not found");
        }
        if (userRepository.get(transaction.getSeller().getId()) == null) {
            throw new EntityNotFoundException("Seller not found");
        }

        String sql = "INSERT INTO TRANSACTION (TRANSACTION_DATE, MATERIAL_NAME, BUYER_ID, SELLER_ID) VALUES (?, ?, ?, ?)";
        try (var connection = dataSource.getConnection();
             var ps = connection.prepareStatement(sql)) {

            ps.setTimestamp(1, transaction.getTransactionDate());
            ps.setString(2, transaction.getMaterialName());
            ps.setInt(3, transaction.getBuyer().getId());
            ps.setInt(4, transaction.getSeller().getId());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RepositoryException("Failed to insert transaction");
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
            int rows = ps.executeUpdate();
            if (rows == 0) {
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
                    var transaction = new TransactionImpl();
                    transaction.setId(rs.getInt("TRANSACTION_ID"));
                    transaction.setTransactionDate(rs.getTimestamp("TRANSACTION_DATE"));
                    transaction.setMaterialName(rs.getString("MATERIAL_NAME"));

                    int buyerId = rs.getInt("BUYER_ID");
                    int sellerId = rs.getInt("SELLER_ID");

                    var buyer = userRepository.get(buyerId);
                    var seller = userRepository.get(sellerId);

                    if (buyer == null || seller == null) {
                        throw new EntityNotFoundException("Buyer or Seller not found");
                    }

                    transaction.setBuyer(buyer);
                    transaction.setSeller(seller);

                    return transaction;
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
                var transaction = new TransactionImpl();
                transaction.setId(rs.getInt("TRANSACTION_ID"));
                transaction.setTransactionDate(rs.getTimestamp("TRANSACTION_DATE"));
                transaction.setMaterialName(rs.getString("MATERIAL_NAME"));

                int buyerId = rs.getInt("BUYER_ID");
                int sellerId = rs.getInt("SELLER_ID");

                var buyer = userRepository.get(buyerId);
                var seller = userRepository.get(sellerId);

                if (buyer == null || seller == null) {
                    throw new EntityNotFoundException("Buyer or Seller not found");
                }

                transaction.setBuyer(buyer);
                transaction.setSeller(seller);

                transactions.add(transaction);
            }
            return transactions;

        } catch (SQLException e) {
            throw new RepositoryException("Error getting all transactions", e);
        }
    }

    @Override
    public List<Transaction> findByDateRange(Timestamp start, Timestamp end) {
        if (start == null || end == null) {
            throw new InvalidDataException("Start and End timestamps cannot be null");
        }
        String sql = "SELECT * FROM TRANSACTION WHERE TRANSACTION_DATE BETWEEN ? AND ?";
        List<Transaction> transactions = new ArrayList<>();

        try (var connection = dataSource.getConnection();
             var ps = connection.prepareStatement(sql)) {

            ps.setTimestamp(1, start);
            ps.setTimestamp(2, end);

            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    var transaction = new TransactionImpl();
                    transaction.setId(rs.getInt("TRANSACTION_ID"));
                    transaction.setTransactionDate(rs.getTimestamp("TRANSACTION_DATE"));
                    transaction.setMaterialName(rs.getString("MATERIAL_NAME"));

                    int buyerId = rs.getInt("BUYER_ID");
                    int sellerId = rs.getInt("SELLER_ID");

                    var buyer = userRepository.get(buyerId);
                    var seller = userRepository.get(sellerId);

                    if (buyer == null || seller == null) {
                        throw new EntityNotFoundException("Buyer or Seller not found");
                    }

                    transaction.setBuyer(buyer);
                    transaction.setSeller(seller);

                    transactions.add(transaction);
                }
            }

        } catch (SQLException e) {
            throw new RepositoryException("Error finding transactions by date range", e);
        }

        return transactions;
    }
}
