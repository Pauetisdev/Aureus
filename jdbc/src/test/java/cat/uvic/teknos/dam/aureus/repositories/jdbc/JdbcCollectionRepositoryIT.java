package cat.uvic.teknos.dam.aureus.repositories.jdbc;

import cat.uvic.teknos.dam.aureus.Collection;
import cat.uvic.teknos.dam.aureus.User;
import cat.uvic.teknos.dam.aureus.impl.CollectionImpl;
import cat.uvic.teknos.dam.aureus.impl.UserImpl;
import cat.uvic.teknos.dam.aureus.repositories.CollectionRepository;
import cat.uvic.teknos.dam.aureus.repositories.UserRepository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.DataSource;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.SingleConnectionDataSource;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JdbcCollectionRepositoryIT {

    private DataSource dataSource;
    private CollectionRepository collectionRepository;
    private UserRepository userRepository;

    @BeforeAll
    void setup() throws SQLException {
        // Configuración de conexión H2 in-memory
        String driver = "org.h2.Driver";
        String server = "jdbc:h2:mem:";
        String database = "testdb;DB_CLOSE_DELAY=-1";
        String user = "sa";
        String password = "";

        dataSource = new SingleConnectionDataSource(driver, server, database, user, password);

        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            // Tabla USER
            st.execute("CREATE TABLE USER (" +
                    "USER_ID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "USERNAME VARCHAR(255), " +
                    "EMAIL VARCHAR(255), " +
                    "PASSWORD VARCHAR(255))");

            // Tabla COLLECTION
            st.execute("CREATE TABLE COLLECTION (" +
                    "COLLECTION_ID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "COLLECTION_NAME VARCHAR(255), " +
                    "DESCRIPTION TEXT, " +
                    "USER_ID INT, " +
                    "FOREIGN KEY (USER_ID) REFERENCES USER(USER_ID))");

            // Insertar usuario dummy
            st.execute("INSERT INTO USER (USERNAME, EMAIL, PASSWORD) VALUES ('user1', 'user1@example.com', '1234')");
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
        collection.setId(user.getId()); // ID del usuario

        collectionRepository.save(collection);

        Set<Collection> all = collectionRepository.getAll();
        assertFalse(all.isEmpty(), "Debe haber al menos una colección guardada");

        Collection retrieved = all.iterator().next();
        assertEquals("Monedas Romanas", retrieved.getCollectionName());
        assertEquals("Colección de denarios y áureos.", retrieved.getDescription());
        assertEquals(user.getId(), retrieved.getId());
    }
}
