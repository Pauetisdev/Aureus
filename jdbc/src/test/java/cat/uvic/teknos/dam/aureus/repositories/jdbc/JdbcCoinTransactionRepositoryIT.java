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
    private JdbcUserRepository userRepository;

    private int coinId;
    private int transactionId;

    @BeforeAll
    void setupDatabase() throws Exception {
        // Parámetros de conexión H2 en memoria
        String driver = "org.h2.Driver";
        String server = "jdbc:h2:mem:";
        String database = "testdb;DB_CLOSE_DELAY=-1";
        String user = "sa";
        String password = "";

        // Inicializar dataSource
        dataSource = new SingleConnectionDataSource(driver, server, database, user, password);

        try (var conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE USER (
                    USER_ID INT AUTO_INCREMENT PRIMARY KEY,
                    NAME VARCHAR(100)
                );
            """);

            stmt.execute("""
                CREATE TABLE COIN (
                    COIN_ID INT AUTO_INCREMENT PRIMARY KEY,
                    COIN_NAME VARCHAR(100) NOT NULL,
                    COIN_YEAR INT NOT NULL,
                    COIN_MATERIAL VARCHAR(50) NOT NULL,
                    COIN_WEIGHT DECIMAL(10,2) NOT NULL,
                    COIN_DIAMETER DECIMAL(10,2) NOT NULL,
                    ESTIMATED_VALUE DECIMAL(10,2) NOT NULL,
                    ORIGIN_COUNTRY VARCHAR(50) NOT NULL,
                    HISTORICAL_SIGNIFICANCE TEXT
                );
            """);

            stmt.execute("""
                CREATE TABLE TRANSACTION (
                    TRANSACTION_ID INT AUTO_INCREMENT PRIMARY KEY,
                    TRANSACTION_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    BUYER_ID INT NOT NULL,
                    SELLER_ID INT NOT NULL,
                    FOREIGN KEY (BUYER_ID) REFERENCES USER(USER_ID),
                    FOREIGN KEY (SELLER_ID) REFERENCES USER(USER_ID)
                );
            """);

            stmt.execute("""
                CREATE TABLE COIN_TRANSACTION (
                    COIN_ID INT,
                    TRANSACTION_ID INT,
                    PRIMARY KEY (COIN_ID, TRANSACTION_ID),
                    FOREIGN KEY (COIN_ID) REFERENCES COIN(COIN_ID),
                    FOREIGN KEY (TRANSACTION_ID) REFERENCES TRANSACTION(TRANSACTION_ID)
                );
            """);
        }

        coinRepository = new JdbcCoinRepository(dataSource);
        userRepository = new JdbcUserRepository(dataSource);
        transactionRepository = new JdbcTransactionRepository(dataSource, userRepository);
        coinTransactionRepository = new JdbcCoinTransactionRepository(dataSource);
    }

    @BeforeEach
    void insertData() throws Exception {
        // Crear y guardar moneda
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
                    "INSERT INTO USER (NAME) VALUES (?), (?)", Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, "Comprador");
                stmt.setString(2, "Venedor");
                stmt.executeUpdate();

                try (var rs = stmt.getGeneratedKeys()) {
                    rs.next();
                    buyerId = rs.getInt(1);
                    rs.next();
                    sellerId = rs.getInt(1);
                }
            }

            try (var stmt = conn.prepareStatement(
                    "INSERT INTO TRANSACTION (BUYER_ID, SELLER_ID) VALUES (?, ?)",
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
            stmt.executeUpdate("DELETE FROM TRANSACTION");
            stmt.executeUpdate("DELETE FROM COIN");
            stmt.executeUpdate("DELETE FROM USER");
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
