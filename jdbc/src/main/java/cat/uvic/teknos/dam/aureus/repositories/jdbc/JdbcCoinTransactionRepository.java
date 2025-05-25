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

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class JdbcCoinTransactionRepository implements CoinTransactionRepository {
    private final DataSource dataSource;
    private final CoinRepository coinRepository;
    private final TransactionRepository transactionRepository;

    public JdbcCoinTransactionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        this.coinRepository = coinRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void save(CoinTransaction value) {
        var connection = dataSource.getConnection();
        try (var ps = connection.prepareStatement("INSERT INTO COIN_TRANSACTION (COIN_ID, TRANSACTION_ID) VALUES (?, ?)")) {
            ps.setInt(1, value.getCoin().getId());
            ps.setInt(2, value.getTransaction().getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new CrudException(e);
        }
    }

    @Override
    public void delete(CoinTransaction value) {
        var connection = dataSource.getConnection();
        try (var ps = connection.prepareStatement("DELETE FROM COIN_TRANSACTION WHERE COIN_ID = ? AND TRANSACTION_ID = ?")) {
            ps.setInt(1, value.getCoin().getId());
            ps.setInt(2, value.getTransaction().getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new CrudException(e);
        }
    }

    @Override
    public CoinTransaction get(Integer id) {
        throw new UnsupportedOperationException("CoinTransaction no tiene una PK simple.");
    }

    @Override
    public Set<CoinTransaction> getAll() {
        var coinTransactions = new HashSet<CoinTransaction>();
        var connection = dataSource.getConnection();
        try (var ps = connection.prepareStatement("SELECT * FROM COIN_TRANSACTION")) {
            var rs = ps.executeQuery();
            while (rs.next()) {
                Coin coin = coinRepository.get(rs.getInt("COIN_ID"));
                Transaction transaction = transactionRepository.get(rs.getInt("TRANSACTION_ID"));
                var coinTransaction = new CoinTransactionImpl();
                coinTransaction.setCoin(coin);
                coinTransaction.setTransaction(transaction);
                coinTransactions.add(coinTransaction);
            }
        } catch (SQLException e) {
            throw new CrudException(e);
        }
        return coinTransactions;
    }
}
