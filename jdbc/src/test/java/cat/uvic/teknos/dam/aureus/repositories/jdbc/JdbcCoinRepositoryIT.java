package cat.uvic.teknos.dam.aureus.repositories.jdbc;

import cat.uvic.teknos.dam.aureus.Coin;
import cat.uvic.teknos.dam.aureus.impl.CoinImpl;
import cat.uvic.teknos.dam.aureus.repositories.CoinRepository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.SingleConnectionDataSource;

import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;

class JdbcCoinRepositoryIT {

    private static CoinRepository repository;
    private static Connection connection;
    private static SingleConnectionDataSource dataSource;

    @BeforeAll
    static void setupDatabase() throws SQLException {

        String driver = "h2";
        String server = "mem";
        String database = "testdb;DB_CLOSE_DELAY=-1";
        String user = "sa";
        String password = "";
        var format = "jdbc:%s:%s:%s";

        dataSource = new SingleConnectionDataSource(format, driver, server, database, user, password);

        connection = dataSource.getConnection();
        repository = new JdbcCoinRepository(dataSource);

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS COIN");
            stmt.execute("""
                CREATE TABLE COIN (
                    COIN_ID INT AUTO_INCREMENT PRIMARY KEY,
                    COIN_NAME VARCHAR(100),
                    ORIGIN_COUNTRY VARCHAR(100),
                    COIN_YEAR INT,
                    COIN_MATERIAL VARCHAR(50),
                    COIN_WEIGHT DECIMAL(10, 2),
                    COIN_DIAMETER DECIMAL(10, 2),
                    ESTIMATED_VALUE DECIMAL(10, 2),
                    HISTORICAL_SIGNIFICANCE TEXT,
                    COLLECTION_ID INT
                )
            """);
        }
    }

    @AfterEach
    void cleanup() throws SQLException {
        try (var conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM COIN");
        }
    }

    @Test
    void testSaveAndGet() {
        Coin coin = new CoinImpl();
        coin.setCoinName("Denarius");
        coin.setOriginCountry("Rome");
        coin.setCoinYear(-100);
        coin.setCoinMaterial("Silver");
        coin.setCoinWeight(new BigDecimal("3.5"));
        coin.setCoinDiameter(new BigDecimal("18.0"));
        coin.setEstimatedValue(new BigDecimal("150.00"));
        coin.setHistoricalSignificance("Ancient Roman coin");

        repository.save(coin);

        assertNotNull(coin.getId(), "El ID debería haberse generado y asignado");

        Coin found = repository.get(coin.getId());
        assertNotNull(found, "Debe encontrar la moneda guardada");
        assertEquals("Denarius", found.getCoinName());
        assertEquals("Rome", found.getOriginCountry());
        assertEquals(-100, found.getCoinYear());
    }

    @Test
    void testDelete() {
        Coin coin = new CoinImpl();
        coin.setCoinName("Sestertius");
        coin.setOriginCountry("Rome");
        coin.setCoinYear(200);
        coin.setCoinMaterial("Bronze");
        coin.setCoinWeight(new BigDecimal("25.0"));
        coin.setCoinDiameter(new BigDecimal("32.0"));
        coin.setEstimatedValue(new BigDecimal("50.00"));
        coin.setHistoricalSignificance("Bronze coin");

        repository.save(coin);
        Integer id = coin.getId();
        assertNotNull(repository.get(id));

        repository.delete(coin);
        assertNull(repository.get(id), "La moneda debe haberse borrado");
    }

    @Test
    void testGetAll() {
        Coin c1 = new CoinImpl();
        c1.setCoinName("Coin1");
        c1.setOriginCountry("Greece");
        c1.setCoinYear(100);
        c1.setCoinMaterial("Silver");
        c1.setCoinWeight(BigDecimal.ONE);
        c1.setCoinDiameter(BigDecimal.ONE);
        c1.setEstimatedValue(BigDecimal.ONE);
        c1.setHistoricalSignificance("Test coin 1");

        Coin c2 = new CoinImpl();
        c2.setCoinName("Coin2");
        c2.setOriginCountry("Egypt");
        c2.setCoinYear(200);
        c2.setCoinMaterial("Gold");
        c2.setCoinWeight(BigDecimal.TEN);
        c2.setCoinDiameter(BigDecimal.TEN);
        c2.setEstimatedValue(BigDecimal.TEN);
        c2.setHistoricalSignificance("Test coin 2");

        repository.save(c1);
        repository.save(c2);

        List<Coin> allCoins = repository.getAll();
        assertEquals(2, allCoins.size());
    }


    @Test
    void testFindByMaterial() {
        Coin silverCoin = new CoinImpl();
        silverCoin.setCoinName("SilverCoin");
        silverCoin.setCoinMaterial("Silver");
        silverCoin.setCoinYear(150);
        silverCoin.setOriginCountry("Rome");
        silverCoin.setCoinWeight(BigDecimal.ONE);
        silverCoin.setCoinDiameter(BigDecimal.ONE);
        silverCoin.setEstimatedValue(BigDecimal.ONE);
        silverCoin.setHistoricalSignificance("Silver coin test");

        Coin goldCoin = new CoinImpl();
        goldCoin.setCoinName("GoldCoin");
        goldCoin.setCoinMaterial("Gold");
        goldCoin.setCoinYear(150);
        goldCoin.setOriginCountry("Rome");
        goldCoin.setCoinWeight(BigDecimal.TEN);
        goldCoin.setCoinDiameter(BigDecimal.TEN);
        goldCoin.setEstimatedValue(BigDecimal.TEN);
        goldCoin.setHistoricalSignificance("Gold coin test");

        repository.save(silverCoin);
        repository.save(goldCoin);

        List<Coin> silverCoins = repository.findByMaterial("Silver");
        assertEquals(1, silverCoins.size());
        assertEquals("SilverCoin", silverCoins.get(0).getCoinName());
    }

    @Test
    void testFindByYear() {
        Coin coin150 = new CoinImpl();
        coin150.setCoinName("Coin150");
        coin150.setCoinMaterial("Gold");
        coin150.setCoinYear(150);
        coin150.setOriginCountry("Rome");
        coin150.setCoinWeight(BigDecimal.ONE);
        coin150.setCoinDiameter(BigDecimal.ONE);
        coin150.setEstimatedValue(BigDecimal.ONE);
        coin150.setHistoricalSignificance("Year 150 coin");

        Coin coin200 = new CoinImpl();
        coin200.setCoinName("Coin200");
        coin200.setCoinMaterial("Silver");
        coin200.setCoinYear(200);
        coin200.setOriginCountry("Rome");
        coin200.setCoinWeight(BigDecimal.TEN);
        coin200.setCoinDiameter(BigDecimal.TEN);
        coin200.setEstimatedValue(BigDecimal.TEN);
        coin200.setHistoricalSignificance("Year 200 coin");

        repository.save(coin150);
        repository.save(coin200);

        List<Coin> coins150 = repository.findByYear(150);
        assertEquals(1, coins150.size());
        assertEquals("Coin150", coins150.get(0).getCoinName());
    }
}