package cat.uvic.teknos.dam.aureus.repositories.jdbc;

import cat.uvic.teknos.dam.aureus.Coin;
import cat.uvic.teknos.dam.aureus.CoinTransaction;
import cat.uvic.teknos.dam.aureus.Transaction;
import cat.uvic.teknos.dam.aureus.impl.CoinImpl;
import cat.uvic.teknos.dam.aureus.impl.CoinTransactionImpl;
import cat.uvic.teknos.dam.aureus.impl.TransactionImpl;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.SingleConnectionDataSource;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.sql.Statement;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JdbcCoinTransactionRepositoryIT {

    private SingleConnectionDataSource dataSource;
    private JdbcCoinRepository coinRepository;
    private JdbcTransactionRepository transactionRepository;
    private JdbcCoinTransactionRepository coinTransactionRepository;

    private int coinId;
    private int transactionId;

    @BeforeAll
    void setupDatabase() throws Exception {
        dataSource = new SingleConnectionDataSource(
                "org.h2.Driver",
                "jdbc:h2:mem:",
                "testdb;DB_CLOSE_DELAY=-1",
                "sa",
                ""
        );

        try (var conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                    CREATE TABLE COIN (
                        ID INT AUTO_INCREMENT PRIMARY KEY,
                        NAME VARCHAR(255),
                        YEAR INT,
                        MATERIAL VARCHAR(255),
                        COIN_WEIGHT DECIMAL,
                        COIN_DIAMETER DECIMAL,
                        ESTIMATED_VALUE DECIMAL,
                        ORIGIN_COUNTRY VARCHAR(255),
                        HISTORICAL_SIGNIFICANCE VARCHAR(255)
                    );
                    """);

            // Cambio nombre tabla de TRANSACTION a TRANSACTIONS para evitar palabra reservada
            stmt.execute("""
                    CREATE TABLE TRANSACTIONS (
                        ID INT AUTO_INCREMENT PRIMARY KEY,
                        TYPE VARCHAR(255),
                        DATE DATE,
                        LOCATION VARCHAR(255),
                        NOTES VARCHAR(255)
                    );
                    """);

            stmt.execute("""
                    CREATE TABLE COIN_TRANSACTION (
                        COIN_ID INT,
                        TRANSACTION_ID INT,
                        PRIMARY KEY (COIN_ID, TRANSACTION_ID),
                        FOREIGN KEY (COIN_ID) REFERENCES COIN(ID),
                        FOREIGN KEY (TRANSACTION_ID) REFERENCES TRANSACTIONS(ID)
                    );
                    """);
        }

        coinRepository = new JdbcCoinRepository(dataSource);
        transactionRepository = new JdbcTransactionRepository(dataSource);
        coinTransactionRepository = new JdbcCoinTransactionRepository(dataSource);
    }

    @BeforeEach
    void insertData() {
        CoinImpl coin = new CoinImpl();
        coin.setCoinName("Denario");
        coin.setCoinYear(100); // ejemplo de año
        coin.setOriginCountry("Roma");
        coin.setCoinMaterial("Argent");
        coin.setCoinWeight(new BigDecimal("3.5"));
        coin.setCoinDiameter(new BigDecimal("19.0"));
        coin.setEstimatedValue(new BigDecimal("500"));
        coin.setHistoricalSignificance("Moneda romana antigua");

        coinRepository.save(coin);
        coinId = coin.getId();

        TransactionImpl transaction = new TransactionImpl();
        transaction.setType("Compra");
        transaction.setDate(java.sql.Date.valueOf("2024-01-01"));
        transaction.setLocation("Barcino");
        transaction.setNotes("Compra mercat antic");

        transactionRepository.save(transaction);
        transactionId = transaction.getId();
    }

    @AfterEach
    void clearTables() throws Exception {
        try (var conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM COIN_TRANSACTION");
            stmt.executeUpdate("DELETE FROM COIN");
            stmt.executeUpdate("DELETE FROM TRANSACTIONS");
        }
    }

    @Test
    void testSaveAndGetAll() {
        Coin coin = coinRepository.get(coinId);
        Transaction transaction = transactionRepository.get(transactionId);

        CoinTransactionImpl coinTransaction = new CoinTransactionImpl();
        coinTransaction.setCoin(coin);
        coinTransaction.setTransaction(transaction);

        coinTransactionRepository.save(coinTransaction);

        Set<CoinTransaction> result = coinTransactionRepository.getAll();
        assertEquals(1, result.size());

        CoinTransaction ct = result.iterator().next();
        assertEquals(coin.getId(), ct.getCoin().getId());
        assertEquals(transaction.getId(), ct.getTransaction().getId());
    }

    @Test
    void testDelete() {
        Coin coin = coinRepository.get(coinId);
        Transaction transaction = transactionRepository.get(transactionId);

        CoinTransactionImpl coinTransaction = new CoinTransactionImpl();
        coinTransaction.setCoin(coin);
        coinTransaction.setTransaction(transaction);

        coinTransactionRepository.save(coinTransaction);
        coinTransactionRepository.delete(coinTransaction);

        Set<CoinTransaction> result = coinTransactionRepository.getAll();
        assertTrue(result.isEmpty());
    }
}
