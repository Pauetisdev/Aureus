package cat.uvic.teknos.dam.aureus.repositories.jdbc;

import cat.uvic.teknos.dam.aureus.Coin;
import cat.uvic.teknos.dam.aureus.CoinTransaction;
import cat.uvic.teknos.dam.aureus.Transaction;
import cat.uvic.teknos.dam.aureus.impl.CoinImpl;
import cat.uvic.teknos.dam.aureus.impl.CoinTransactionImpl;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.TestSingleConnectionDataSource;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JdbcCoinTransactionRepositoryIT {

    private TestSingleConnectionDataSource dataSource;
    private JdbcCoinRepository coinRepository;
    private JdbcTransactionRepository transactionRepository;
    private JdbcCoinTransactionRepository coinTransactionRepository;
    private JdbcUserRepository userRepository;

    private int coinId;
    private int transactionId;

    @BeforeAll
    void setupDatabase() throws Exception {
        String driver = "h2";
        String server = "mem";
        String database = "testdb;DB_CLOSE_DELAY=-1";
        String user = "sa";
        String password = "";
        var format = "jdbc:%s:%s:%s";

        dataSource = new TestSingleConnectionDataSource(format, driver, server, database, user, password);

        try (var conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            // Ensure clean state: drop dependents first
            stmt.execute("DROP TABLE IF EXISTS COIN_TRANSACTION");
            stmt.execute("DROP TABLE IF EXISTS COIN_COLLECTION");
            stmt.execute("DROP TABLE IF EXISTS COIN");
            stmt.execute("DROP TABLE IF EXISTS COLLECTION");
            stmt.execute("DROP TABLE IF EXISTS \"TRANSACTION\"");
            stmt.execute("DROP TABLE IF EXISTS \"USER\"");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS "USER" (
                    USER_ID INT AUTO_INCREMENT PRIMARY KEY,
                    USERNAME VARCHAR(100) NOT NULL,
                    EMAIL VARCHAR(100) NOT NULL,
                    PASSWORD_HASH VARCHAR(100) NOT NULL,
                    JOIN_DATE TIMESTAMP NOT NULL
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS COIN (
                    COIN_ID INT AUTO_INCREMENT PRIMARY KEY,
                    COIN_NAME VARCHAR(100) NOT NULL,
                    COIN_YEAR INT NOT NULL,
                    COIN_MATERIAL VARCHAR(50) NOT NULL,
                    COIN_WEIGHT DECIMAL(10,2) NOT NULL,
                    COIN_DIAMETER DECIMAL(10,2) NOT NULL,
                    ESTIMATED_VALUE DECIMAL(10,2) NOT NULL,
                    ORIGIN_COUNTRY VARCHAR(50) NOT NULL,
                    HISTORICAL_SIGNIFICANCE TEXT,
                    COLLECTION_ID INT
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS "TRANSACTION" (
                    TRANSACTION_ID INT AUTO_INCREMENT PRIMARY KEY,
                    TRANSACTION_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    BUYER_ID INT NOT NULL,
                    SELLER_ID INT NOT NULL,
                    FOREIGN KEY (BUYER_ID) REFERENCES "USER"(USER_ID),
                    FOREIGN KEY (SELLER_ID) REFERENCES "USER"(USER_ID)
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS COIN_TRANSACTION (
                    COIN_ID INT,
                    TRANSACTION_ID INT,
                    TRANSACTION_PRICE DECIMAL(15,4),
                    CURRENCY VARCHAR(10),
                    PRIMARY KEY (COIN_ID, TRANSACTION_ID),
                    FOREIGN KEY (COIN_ID) REFERENCES COIN(COIN_ID),
                    FOREIGN KEY (TRANSACTION_ID) REFERENCES "TRANSACTION"(TRANSACTION_ID)
                );
            """);
        }

        coinRepository = new JdbcCoinRepository(dataSource);
        userRepository = new JdbcUserRepository(dataSource);
        transactionRepository = new JdbcTransactionRepository(dataSource, userRepository);
        coinTransactionRepository = new JdbcCoinTransactionRepository(dataSource, coinRepository, transactionRepository);
    }

    @BeforeEach
    void insertData() throws Exception {
        CoinImpl coin = new CoinImpl();
        coin.setCoinName("Denario");
        coin.setCoinYear(100);
        coin.setOriginCountry("Roma");
        coin.setCoinMaterial("Argent");
        coin.setCoinWeight(new BigDecimal("3.5"));
        coin.setCoinDiameter(new BigDecimal("19.0"));
        coin.setEstimatedValue(new BigDecimal("500"));
        coin.setHistoricalSignificance("Moneda romana antiga");

        coinRepository.save(coin);
        coinId = coin.getId();

        try (var conn = dataSource.getConnection()) {
            int buyerId;
            int sellerId;

            try (var stmt = conn.prepareStatement(
                    "INSERT INTO \"USER\" (USERNAME, EMAIL, PASSWORD_HASH, JOIN_DATE) VALUES (?, ?, ?, ?), (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, "Comprador");
                stmt.setString(2, "comprador@email.com");
                stmt.setString(3, "password1");
                stmt.setTimestamp(4, java.sql.Timestamp.valueOf("2024-01-01 00:00:00"));
                stmt.setString(5, "Venedor");
                stmt.setString(6, "venedor@email.com");
                stmt.setString(7, "password2");
                stmt.setTimestamp(8, java.sql.Timestamp.valueOf("2024-01-02 00:00:00"));
                stmt.executeUpdate();

                try (var rs = stmt.getGeneratedKeys()) {
                    rs.next();
                    buyerId = rs.getInt(1);
                    rs.next();
                    sellerId = rs.getInt(1);
                }
            }

            try (var stmt = conn.prepareStatement(
                    "INSERT INTO \"TRANSACTION\" (BUYER_ID, SELLER_ID) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, buyerId);
                stmt.setInt(2, sellerId);
                stmt.executeUpdate();

                try (var rs = stmt.getGeneratedKeys()) {
                    rs.next();
                    transactionId = rs.getInt(1);
                }
            }
        }
    }

    @AfterEach
    void clearTables() throws Exception {
        try (var conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM COIN_TRANSACTION");
            stmt.executeUpdate("DELETE FROM \"TRANSACTION\"");
            stmt.executeUpdate("DELETE FROM COIN");
            stmt.executeUpdate("DELETE FROM \"USER\"");
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

        List<CoinTransaction> result = coinTransactionRepository.getAll();
        assertEquals(1, result.size());

        CoinTransaction ct = result.get(0);
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

        List<CoinTransaction> result = coinTransactionRepository.getAll();
        assertTrue(result.isEmpty());
    }
}
