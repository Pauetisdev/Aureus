package cat.uvic.teknos.dam.aureus.repositories.jdbc;

import cat.uvic.teknos.dam.aureus.Transaction;
import cat.uvic.teknos.dam.aureus.impl.TransactionImpl;
import cat.uvic.teknos.dam.aureus.repositories.TransactionRepository;
import cat.uvic.teknos.dam.aureus.repositories.UserRepository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.DataSource;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.CrudException;

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
        this.dataSource = dataSource;
        this.userRepository = userRepository;
    }

    public JdbcTransactionRepository(DataSource dataSource) {
        DataSource dataSource1 = null;
        this.dataSource = dataSource1;
        this.userRepository = userRepository;
    }

    @Override
    public void save(Transaction transaction) {
        var connection = dataSource.getConnection();
        try (var ps = connection.prepareStatement(
                "INSERT INTO TRANSACTION (TRANSACTION_DATE, MATERIAL_NAME, BUYER_ID, SELLER_ID) VALUES (?, ?, ?, ?)"
        )) {
            ps.setTimestamp(1, transaction.getTransactionDate());
            ps.setString(2, transaction.getMaterialName());
            ps.setInt(3, transaction.getBuyer().getId());
            ps.setInt(4, transaction.getSeller().getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new CrudException(e);
        }
    }

    @Override
    public void delete(Transaction transaction) {
        var connection = dataSource.getConnection();
        try (var ps = connection.prepareStatement("DELETE FROM TRANSACTION WHERE TRANSACTION_ID = ?")) {
            ps.setInt(1, transaction.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new CrudException(e);
        }
    }

    @Override
    public Transaction get(Integer id) {
        var connection = dataSource.getConnection();
        try (var ps = connection.prepareStatement("SELECT * FROM TRANSACTION WHERE TRANSACTION_ID = ?")) {
            ps.setInt(1, id);
            var rs = ps.executeQuery();
            if (rs.next()) {
                var transaction = new TransactionImpl();
                transaction.setId(rs.getInt("TRANSACTION_ID"));
                transaction.setTransactionDate(rs.getTimestamp("TRANSACTION_DATE"));
                transaction.setMaterialName(rs.getString("MATERIAL_NAME"));

                // ⚠️ Obtenemos los objetos User usando los IDs
                int buyerId = rs.getInt("BUYER_ID");
                int sellerId = rs.getInt("SELLER_ID");
                transaction.setBuyer(userRepository.get(buyerId));
                transaction.setSeller(userRepository.get(sellerId));

                return transaction;
            }
        } catch (SQLException e) {
            throw new CrudException(e);
        }
        return null;
    }

    @Override
    public Set<Transaction> getAll() {
        var transactions = new HashSet<Transaction>();
        var connection = dataSource.getConnection();
        try (var ps = connection.prepareStatement("SELECT * FROM TRANSACTION")) {
            var rs = ps.executeQuery();
            while (rs.next()) {
                var transaction = new TransactionImpl();
                transaction.setId(rs.getInt("TRANSACTION_ID"));
                transaction.setTransactionDate(rs.getTimestamp("TRANSACTION_DATE"));
                transaction.setMaterialName(rs.getString("MATERIAL_NAME"));

                int buyerId = rs.getInt("BUYER_ID");
                int sellerId = rs.getInt("SELLER_ID");
                transaction.setBuyer(userRepository.get(buyerId));
                transaction.setSeller(userRepository.get(sellerId));

                transactions.add(transaction);
            }
        } catch (SQLException e) {
            throw new CrudException(e);
        }
        return transactions;
    }


    @Override
    public List<Transaction> findByDateRange(Timestamp start, Timestamp end) {
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

                    transaction.setBuyer(userRepository.get(buyerId));
                    transaction.setSeller(userRepository.get(sellerId));

                    transactions.add(transaction);
                }
            }

        } catch (SQLException e) {
            throw new CrudException("Error finding Transactions by date range", e);
        }

        return transactions;
    }
}
