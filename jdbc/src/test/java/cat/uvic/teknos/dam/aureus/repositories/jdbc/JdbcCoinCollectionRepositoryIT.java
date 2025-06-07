package cat.uvic.teknos.dam.aureus.repositories.jdbc;

import cat.uvic.teknos.dam.aureus.Coin;
import cat.uvic.teknos.dam.aureus.Collection;
import cat.uvic.teknos.dam.aureus.CoinCollection;
import cat.uvic.teknos.dam.aureus.impl.CoinCollectionImpl;
import cat.uvic.teknos.dam.aureus.repositories.CoinRepository;
import cat.uvic.teknos.dam.aureus.repositories.CollectionRepository;
import cat.uvic.teknos.dam.aureus.repositories.UserRepository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.SingleConnectionDataSource;
import org.junit.jupiter.api.*;

import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JdbcCoinCollectionRepositoryIT {
    private SingleConnectionDataSource dataSource;
    private CoinRepository coinRepository;
    private CollectionRepository collectionRepository;
    private JdbcCoinCollectionRepository repo;
    private UserRepository userRepository;

    @BeforeAll
    void setupDatabase() throws Exception {
        String driver = "h2";
        String server = "mem";
        String database = "testdb;DB_CLOSE_DELAY=-1";
        String user = "sa";
        String password = "";
        var format = "jdbc:%s:%s:%s";

        dataSource = new SingleConnectionDataSource(format, driver, server, database, user, password);

        userRepository = new JdbcUserRepository(dataSource);
        coinRepository = new JdbcCoinRepository(dataSource);
        collectionRepository = new JdbcCollectionRepository(dataSource, userRepository);
        repo = new JdbcCoinCollectionRepository(dataSource, coinRepository, collectionRepository);

        Statement st = dataSource.getConnection().createStatement();

        st.execute("""
            CREATE TABLE "USER" (
                USER_ID INT AUTO_INCREMENT PRIMARY KEY,
                USERNAME VARCHAR(50) NOT NULL UNIQUE,
                EMAIL VARCHAR(100) NOT NULL UNIQUE,
                PASSWORD_HASH VARCHAR(64) NOT NULL,
                JOIN_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);
        st.execute("""
            CREATE TABLE COLLECTION (
                COLLECTION_ID INT AUTO_INCREMENT PRIMARY KEY,
                COLLECTION_NAME VARCHAR(100) NOT NULL,
                DESCRIPTION TEXT,
                USER_ID INT,
                FOREIGN KEY (USER_ID) REFERENCES "USER"(USER_ID) ON DELETE CASCADE
            )
        """);
        st.execute("""
            CREATE TABLE COIN (
                COIN_ID INT AUTO_INCREMENT PRIMARY KEY,
                COIN_NAME VARCHAR(100) NOT NULL,
                COIN_YEAR INT NOT NULL,
                COIN_MATERIAL VARCHAR(50) NOT NULL,
                COIN_WEIGHT DECIMAL(10,2) NOT NULL,
                COIN_DIAMETER DECIMAL(10,2) NOT NULL,
                ESTIMATED_VALUE DECIMAL(10,2) NOT NULL,
                ORIGIN_COUNTRY VARCHAR(50) NOT NULL,
                HISTORICAL_SIGNIFICANCE TEXT,
                COLLECTION_ID INT,
                FOREIGN KEY (COLLECTION_ID) REFERENCES COLLECTION(COLLECTION_ID) ON DELETE SET NULL
            )
        """);
        st.execute("""
            CREATE TABLE COIN_COLLECTION (
                COIN_ID INT,
                COLLECTION_ID INT,
                PRIMARY KEY (COIN_ID, COLLECTION_ID),
                FOREIGN KEY (COIN_ID) REFERENCES COIN(COIN_ID) ON DELETE CASCADE,
                FOREIGN KEY (COLLECTION_ID) REFERENCES COLLECTION(COLLECTION_ID) ON DELETE CASCADE
            )
        """);
        st.execute("""
            INSERT INTO "USER" (USERNAME, EMAIL, PASSWORD_HASH)
            VALUES ('testuser', 'test@example.com', 'hash123')
        """);
        st.execute("""
            INSERT INTO COLLECTION (COLLECTION_NAME, DESCRIPTION, USER_ID)
            VALUES ('Test Collection', 'Test Description', 1)
        """);
        st.execute("""
            INSERT INTO COIN (
                COIN_NAME, COIN_YEAR, COIN_MATERIAL, COIN_WEIGHT,
                COIN_DIAMETER, ESTIMATED_VALUE, ORIGIN_COUNTRY,
                HISTORICAL_SIGNIFICANCE, COLLECTION_ID
            ) VALUES (
                'Test Coin', 1900, 'Gold', 5.5,
                20.0, 1500.00, 'Spain',
                'Test Historical Significance', 1
            )
        """);
    }

    @BeforeEach
    void cleanUpCoinCollection() throws Exception {
        Statement st = dataSource.getConnection().createStatement();
        st.execute("DELETE FROM COIN_COLLECTION");
    }

    @AfterAll
    void tearDown() throws Exception {
        Statement st = dataSource.getConnection().createStatement();
        st.execute("DROP TABLE IF EXISTS COIN_COLLECTION");
        st.execute("DROP TABLE IF EXISTS COIN");
        st.execute("DROP TABLE IF EXISTS COLLECTION");
        st.execute("DROP TABLE IF EXISTS \"USER\"");
        dataSource.close();
    }

    @Test
    void testSaveAndFindByCollectionId() {
        Coin coin = coinRepository.get(1);
        Collection collection = collectionRepository.get(1);

        assertNotNull(coin, "La moneda no debe ser nula");
        assertNotNull(collection, "La colección no debe ser nula");

        CoinCollection cc = new CoinCollectionImpl();
        cc.setCoin(coin);
        cc.setCollection(collection);

        repo.save(cc);

        List<CoinCollection> list = repo.findByCollectionId(collection.getId());

        assertFalse(list.isEmpty(), "La lista no debe estar vacía");
        assertEquals(1, list.size(), "Debe haber un único elemento");
        assertEquals(coin.getId(), list.get(0).getCoin().getId(), "Coin ID debe coincidir");
        assertEquals(collection.getId(), list.get(0).getCollection().getId(), "Collection ID debe coincidir");
    }

    @Test
    void testFindByCoinId() {
        Coin coin = coinRepository.get(1);
        Collection collection = collectionRepository.get(1);

        assertNotNull(coin, "La moneda no debe ser nula");
        assertNotNull(collection, "La colección no debe ser nula");

        CoinCollection cc = new CoinCollectionImpl();
        cc.setCoin(coin);
        cc.setCollection(collection);

        repo.save(cc);

        List<CoinCollection> list = repo.findByCoinId(coin.getId());

        assertFalse(list.isEmpty(), "La lista no debe estar vacía");
        assertEquals(1, list.size(), "Debe haber un único elemento");
        assertEquals(coin.getId(), list.get(0).getCoin().getId(), "Coin ID debe coincidir");
        assertEquals(collection.getId(), list.get(0).getCollection().getId(), "Collection ID debe coincidir");
    }

    @Test
    void testDelete() {
        Coin coin = coinRepository.get(1);
        Collection collection = collectionRepository.get(1);

        assertNotNull(coin, "La moneda no debe ser nula");
        assertNotNull(collection, "La colección no debe ser nula");

        CoinCollection cc = new CoinCollectionImpl();
        cc.setCoin(coin);
        cc.setCollection(collection);

        repo.save(cc);

        repo.delete(cc);

        List<CoinCollection> list = repo.findByCollectionId(collection.getId());
        assertTrue(list.isEmpty(), "La lista debe estar vacía después de eliminar");
    }

    @Test
    void testGetAll() {
        Coin coin = coinRepository.get(1);
        Collection collection = collectionRepository.get(1);

        assertNotNull(coin, "La moneda no debe ser nula");
        assertNotNull(collection, "La colección no debe ser nula");

        CoinCollection cc = new CoinCollectionImpl();
        cc.setCoin(coin);
        cc.setCollection(collection);

        repo.save(cc);

        var allCoinCollections = repo.getAll();

        assertFalse(allCoinCollections.isEmpty(), "La lista no debe estar vacía");
        assertEquals(1, allCoinCollections.size(), "Debe haber un único elemento");

        CoinCollection retrieved = allCoinCollections.iterator().next();
        assertEquals(coin.getId(), retrieved.getCoin().getId(), "Coin ID debe coincidir");
        assertEquals(collection.getId(), retrieved.getCollection().getId(), "Collection ID debe coincidir");
    }
}