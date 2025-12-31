package cat.uvic.teknos.dam.aureus.repositories.jdbc;

import cat.uvic.teknos.dam.aureus.Collection;
import cat.uvic.teknos.dam.aureus.User;
import cat.uvic.teknos.dam.aureus.impl.CollectionImpl;
import cat.uvic.teknos.dam.aureus.impl.UserImpl;
import cat.uvic.teknos.dam.aureus.repositories.CollectionRepository;
import cat.uvic.teknos.dam.aureus.repositories.UserRepository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.DataSource;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.TestSingleConnectionDataSource;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JdbcCollectionRepositoryIT {

    private TestSingleConnectionDataSource dataSource;
    private CollectionRepository collectionRepository;
    private UserRepository userRepository;

    @BeforeAll
    void setup() throws SQLException {
        // Configuración de conexión H2 in-memory
        String driver = "h2";
        String server = "mem";
        String database = "testdb;DB_CLOSE_DELAY=-1";
        String user = "sa";
        String password = "";
        var format = "jdbc:%s:%s:%s";

        dataSource = new TestSingleConnectionDataSource(format, driver, server, database, user, password);

        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            // Ensure clean state
            st.execute("DROP TABLE IF EXISTS COLLECTION");
            st.execute("DROP TABLE IF EXISTS \"USER\"");

            // Tabla USER
            st.execute("CREATE TABLE IF NOT EXISTS \"USER\" (" +
                    "USER_ID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "USERNAME VARCHAR(255), " +
                    "EMAIL VARCHAR(255), " +
                    "PASSWORD_HASH VARCHAR(255), " +
                    "JOIN_DATE TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS COLLECTION (" +
                    "COLLECTION_ID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "COLLECTION_NAME VARCHAR(255), " +
                    "DESCRIPTION TEXT, " +
                    "USER_ID INT, " +
                    "FOREIGN KEY (USER_ID) REFERENCES \"USER\"(USER_ID))");

            st.execute("INSERT INTO \"USER\" (USERNAME, EMAIL, PASSWORD_HASH, JOIN_DATE) VALUES ('user1', 'user1@example.com', '1234', CURRENT_TIMESTAMP)");
        }

        userRepository = new JdbcUserRepository(dataSource);
        collectionRepository = new JdbcCollectionRepository(dataSource, userRepository);
    }

    @Test
    void testSaveAndGetCollection() {
        User user = userRepository.get(1);

        Collection collection = new CollectionImpl();
        collection.setCollectionName("Monedas Romanas");
        collection.setDescription("Colección de denarios y áureos.");
        collection.setUser(user);

        collectionRepository.save(collection);

        List<Collection> all = collectionRepository.getAll();
        assertFalse(all.isEmpty(), "Debe haber al menos una colección guardada");

        Collection retrieved = all.get(0);
        assertEquals("Monedas Romanas", retrieved.getCollectionName());
        assertEquals("Colección de denarios y áureos.", retrieved.getDescription());
        assertEquals(user.getId(), retrieved.getUser().getId());
    }
}
