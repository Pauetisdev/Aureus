package cat.uvic.teknos.dam.aureus.repositories.jdbc;

import cat.uvic.teknos.dam.aureus.User;
import cat.uvic.teknos.dam.aureus.impl.UserImpl;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.SingleConnectionDataSource;
import org.junit.jupiter.api.*;

import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JdbcUserRepositoryIT {

    private SingleConnectionDataSource dataSource;
    private JdbcUserRepository userRepository;

    @BeforeAll
    void setupDatabase() throws Exception {
        String driver = "h2";
        String server = "mem";
        String database = "testdbUser;DB_CLOSE_DELAY=-1";
        String user = "sa";
        String password = "";
        var format = "jdbc:%s:%s:%s";

        dataSource = new SingleConnectionDataSource(format, driver, server, database, user, password);

        try (var conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE "USER" (
                    USER_ID INT AUTO_INCREMENT PRIMARY KEY,
                    USERNAME VARCHAR(100) NOT NULL,
                    EMAIL VARCHAR(100) UNIQUE NOT NULL,
                    PASSWORD_HASH VARCHAR(255) NOT NULL,
                    JOIN_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);
        }

        userRepository = new JdbcUserRepository(dataSource);
    }

    @AfterEach
    void clearTable() throws Exception {
        try (var conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM \"USER\"");
        }
    }

    @Test
    void testSaveAndGet() {
        UserImpl user = new UserImpl();
        user.setUsername("johndoe");
        user.setEmail("john@example.com");
        user.setPasswordHash("hashed_pwd");

        userRepository.save(user);
        assertNotNull(userRepository.findByEmail("john@example.com"));

        var fetched = userRepository.getAll().get(0);
        assertEquals(user.getUsername(), fetched.getUsername());
        assertEquals(user.getEmail(), fetched.getEmail());
        assertEquals(user.getPasswordHash(), fetched.getPasswordHash());
    }

    @Test
    void testUpdate() {
        UserImpl user = new UserImpl();
        user.setUsername("janedoe");
        user.setEmail("jane@example.com");
        user.setPasswordHash("pwd123");

        userRepository.save(user);

        var saved = userRepository.findByEmail("jane@example.com");
        assertNotNull(saved);
        saved.setUsername("janedoe_updated");
        saved.setPasswordHash("new_pwd");
        userRepository.save(saved);

        var updated = userRepository.findByEmail("jane@example.com");
        assertEquals("janedoe_updated", updated.getUsername());
        assertEquals("new_pwd", updated.getPasswordHash());
    }

    @Test
    void testDelete() {
        UserImpl user = new UserImpl();
        user.setUsername("todelete");
        user.setEmail("delete@example.com");
        user.setPasswordHash("to_be_deleted");

        userRepository.save(user);

        User saved = userRepository.findByEmail("delete@example.com");
        assertNotNull(saved);

        userRepository.delete(saved);
        assertNull(userRepository.findByEmail("delete@example.com"));
    }

    @Test
    void testGetAll() {
        UserImpl user1 = new UserImpl();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setPasswordHash("pwd1");

        UserImpl user2 = new UserImpl();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPasswordHash("pwd2");

        userRepository.save(user1);
        userRepository.save(user2);

        List<User> users = userRepository.getAll();
        assertEquals(2, users.size());
    }

    @Test
    void testGetById() {
        UserImpl user = new UserImpl();
        user.setUsername("unique");
        user.setEmail("unique@example.com");
        user.setPasswordHash("unique_pwd");

        userRepository.save(user);
        User saved = userRepository.findByEmail("unique@example.com");

        assertNotNull(saved);
        User fetched = userRepository.get(saved.getId());
        assertNotNull(fetched);
        assertEquals(saved.getUsername(), fetched.getUsername());
    }
}
