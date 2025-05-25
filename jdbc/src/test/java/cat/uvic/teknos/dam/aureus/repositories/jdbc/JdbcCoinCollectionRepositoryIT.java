package cat.uvic.teknos.dam.aureus.repositories.jdbc;

import cat.uvic.teknos.dam.aureus.Coin;
import cat.uvic.teknos.dam.aureus.Collection;
import cat.uvic.teknos.dam.aureus.CoinCollection;
import cat.uvic.teknos.dam.aureus.impl.CoinCollectionImpl;
import cat.uvic.teknos.dam.aureus.repositories.CoinRepository;
import cat.uvic.teknos.dam.aureus.repositories.CollectionRepository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.DataSource;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.SingleConnectionDataSource;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JdbcCoinCollectionRepositoryIT {

    static DataSource dataSource;
    static CoinRepository coinRepository;
    static CollectionRepository collectionRepository;
    static JdbcCoinCollectionRepository repo;

    @BeforeAll
    static void setupDatabase() throws Exception {

        // Parámetros de conexión H2 en memoria
        String driver = "org.h2.Driver";
        String server = "jdbc:h2:mem:";
        String database = "testdb;DB_CLOSE_DELAY=-1";
        String user = "sa";
        String password = "";

        // Inicializamos SingleConnectionDataSource con los parámetros
        dataSource = new SingleConnectionDataSource(driver, server, database, user, password);

        coinRepository = new JdbcCoinRepository(dataSource);
        collectionRepository = new JdbcCollectionRepository(dataSource);
        repo = new JdbcCoinCollectionRepository(dataSource);

        try (Connection con = dataSource.getConnection();
             Statement st = con.createStatement()) {

            // Crear tablas USER, COLLECTION, COIN y COIN_COLLECTION
            st.execute("CREATE TABLE USER (" +
                    "USER_ID INT AUTO_INCREMENT PRIMARY KEY, " +
                    "USERNAME VARCHAR(50) NOT NULL UNIQUE, " +
                    "EMAIL VARCHAR(100) NOT NULL UNIQUE, " +
                    "PASSWORD_HASH VARCHAR(64) NOT NULL, " +
                    "JOIN_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            st.execute("CREATE TABLE COLLECTION (" +
                    "COLLECTION_ID INT AUTO_INCREMENT PRIMARY KEY, " +
                    "COLLECTION_NAME VARCHAR(100) NOT NULL, " +
                    "DESCRIPTION TEXT, " +
                    "USER_ID INT, " +
                    "FOREIGN KEY (USER_ID) REFERENCES USER(USER_ID) ON DELETE CASCADE" +
                    ")");

            st.execute("CREATE TABLE COIN (" +
                    "COIN_ID INT AUTO_INCREMENT PRIMARY KEY, " +
                    "COIN_NAME VARCHAR(100) NOT NULL, " +
                    "COIN_YEAR INT NOT NULL, " +
                    "COIN_MATERIAL VARCHAR(50) NOT NULL, " +
                    "COIN_WEIGHT DECIMAL(10,2) NOT NULL, " +
                    "COIN_DIAMETER DECIMAL(10,2) NOT NULL, " +
                    "ESTIMATED_VALUE DECIMAL(10,2) NOT NULL, " +
                    "ORIGIN_COUNTRY VARCHAR(50) NOT NULL, " +
                    "HISTORICAL_SIGNIFICANCE TEXT, " +
                    "COLLECTION_ID INT, " +
                    "FOREIGN KEY (COLLECTION_ID) REFERENCES COLLECTION(COLLECTION_ID) ON DELETE SET NULL" +
                    ")");

            st.execute("CREATE TABLE COIN_COLLECTION (" +
                    "COIN_ID INT, " +
                    "COLLECTION_ID INT, " +
                    "PRIMARY KEY (COIN_ID, COLLECTION_ID), " +
                    "FOREIGN KEY (COIN_ID) REFERENCES COIN(COIN_ID) ON DELETE CASCADE, " +
                    "FOREIGN KEY (COLLECTION_ID) REFERENCES COLLECTION(COLLECTION_ID) ON DELETE CASCADE" +
                    ")");

            // Insertar datos de prueba
            st.execute("INSERT INTO USER (USERNAME, EMAIL, PASSWORD_HASH) VALUES ('testuser', 'testuser@example.com', 'hashpass')");
            st.execute("INSERT INTO COLLECTION (COLLECTION_NAME, DESCRIPTION, USER_ID) VALUES ('Colección 1', 'Descripción test', 1)");
            st.execute("INSERT INTO COIN (COIN_NAME, COIN_YEAR, COIN_MATERIAL, COIN_WEIGHT, COIN_DIAMETER, ESTIMATED_VALUE, ORIGIN_COUNTRY, HISTORICAL_SIGNIFICANCE, COLLECTION_ID) " +
                    "VALUES ('Moneda 1', 1900, 'Oro', 5.5, 20.0, 1500.00, 'España', 'Moneda histórica', 1)");
        }
    }

    @AfterAll
    static void tearDown() throws Exception {
        try (Connection con = dataSource.getConnection();
             Statement st = con.createStatement()) {
            st.execute("DROP TABLE COIN_COLLECTION");
            st.execute("DROP TABLE COIN");
            st.execute("DROP TABLE COLLECTION");
            st.execute("DROP TABLE USER");
        }
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
}
