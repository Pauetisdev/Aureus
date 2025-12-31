package cat.uvic.teknos.dam.aureus.repositories.jdbc;

import cat.uvic.teknos.dam.aureus.Coin;
import cat.uvic.teknos.dam.aureus.CoinTransaction;
import cat.uvic.teknos.dam.aureus.Transaction;
import cat.uvic.teknos.dam.aureus.impl.CoinTransactionImpl;
import cat.uvic.teknos.dam.aureus.repositories.CoinRepository;
import cat.uvic.teknos.dam.aureus.repositories.CoinTransactionRepository;
import cat.uvic.teknos.dam.aureus.repositories.TransactionRepository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.DataSource;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.CrudException;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.model.JdbcCoinTransaction;

import java.util.List;
import java.util.ArrayList;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class JdbcCoinTransactionRepository implements CoinTransactionRepository {
    private final DataSource dataSource;
    private final CoinRepository coinRepository;
    private final TransactionRepository transactionRepository;

    public JdbcCoinTransactionRepository(DataSource dataSource, CoinRepository coinRepository, TransactionRepository transactionRepository) {
        this.dataSource = dataSource;
        this.coinRepository = coinRepository;
        this.transactionRepository = transactionRepository;
    }

    public JdbcCoinTransactionRepository(DataSource dataSource, DataSource dataSource1, CoinRepository coinRepository, TransactionRepository transactionRepository) {
        this.dataSource = dataSource1;
        this.coinRepository = coinRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void save(CoinTransaction value) {
        String sql = "INSERT INTO COIN_TRANSACTION (COIN_ID, TRANSACTION_ID, TRANSACTION_PRICE, CURRENCY) VALUES (?, ?, ?, ?)";
        try (var connection = dataSource.getConnection();
             var ps = connection.prepareStatement(sql)) {

            ps.setInt(1, value.getCoin().getId());
            ps.setInt(2, value.getTransaction().getId());
            ps.setBigDecimal(3, value.getTransactionPrice());
            ps.setString(4, value.getCurrency());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new CrudException("Error saving CoinTransaction", e);
        }
    }


    @Override
    public void delete(CoinTransaction value) {
        try (var connection = dataSource.getConnection();
             var ps = connection.prepareStatement("DELETE FROM COIN_TRANSACTION WHERE COIN_ID = ? AND TRANSACTION_ID = ?")) {

            ps.setInt(1, value.getCoin().getId());
            ps.setInt(2, value.getTransaction().getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new CrudException("Error deleting CoinTransaction", e);
        }
    }

    @Override
    public CoinTransaction get(Integer id) {
        throw new UnsupportedOperationException("CoinTransaction no tiene una clave primaria simple.");
    }

    @Override
    public List<CoinTransaction> getAll() {
        List<CoinTransaction> coinTransactions = new ArrayList<>();
        String sql = """
        SELECT ct.COIN_ID, ct.TRANSACTION_ID, ct.TRANSACTION_PRICE, ct.CURRENCY
        FROM COIN_TRANSACTION ct
    """;
        try (
                var conn = dataSource.getConnection();
                var stmt = conn.prepareStatement(sql);
                var rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                Coin coin = coinRepository.get(rs.getInt("COIN_ID"));
                Transaction transaction = transactionRepository.get(rs.getInt("TRANSACTION_ID"));

                JdbcCoinTransaction coinTransaction = new JdbcCoinTransaction();
                coinTransaction.setCoin(coin);
                coinTransaction.setTransaction(transaction);
                coinTransaction.setTransactionPrice(rs.getBigDecimal("TRANSACTION_PRICE"));
                coinTransaction.setCurrency(rs.getString("CURRENCY"));

                coinTransactions.add(coinTransaction);
            }
        } catch (SQLException e) {
            throw new CrudException("Error getting all CoinTransactions", e);
        }
        return coinTransactions;
    }


}
