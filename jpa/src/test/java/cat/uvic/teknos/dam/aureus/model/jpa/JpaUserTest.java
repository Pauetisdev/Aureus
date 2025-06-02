package cat.uvic.teknos.dam.aureus.model.jpa;

import cat.uvic.teknos.dam.aureus.User;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.EntityNotFoundException;
import cat.uvic.teknos.dam.aureus.model.jpa.repositories.JpaUserRepository;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JpaUserTest {

    private static EntityManagerFactory entityManagerFactory;
    private JpaUserRepository repository;

    @BeforeAll
    static void setUp() {
        entityManagerFactory = Persistence.createEntityManagerFactory("aureus_test");
    }

    @BeforeEach
    void beforeEach() {
        this.repository = new JpaUserRepository(entityManagerFactory.createEntityManager());
    }

    @AfterAll
    static void tearDown() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Should insert and retrieve a user")
    void shouldInsertAndGetUser() {
        // Arrange
        var user = new JpaUser();
        user.setId(1);
        user.setUsername("john_doe");
        user.setEmail("john.doe@example.com");
        user.setPasswordHash("hash123");
        user.setJoinDate(LocalDateTime.now());

        // Act
        repository.save(user);

        // Assert
        var retrieved = repository.get(user.getId());
        assertNotNull(retrieved);
        assertEquals("john_doe", retrieved.getUsername());
        assertEquals("john.doe@example.com", retrieved.getEmail());
    }

    @Test
    @Order(2)
    @DisplayName("Should update an existing user")
    void shouldUpdateUser() {
        // Arrange
        var user = repository.get(1);
        user.setUsername("updated_username");

        // Act
        repository.save(user);

        // Assert
        var updated = repository.get(1);
        assertNotNull(updated);
        assertEquals("updated_username", updated.getUsername());
    }

    @Test
    @Order(3)
    @DisplayName("Should get all users")
    void shouldGetAllUsers() {
        // Act
        Set<User> users = repository.getAll();

        // Assert
        assertNotNull(users);
        assertFalse(users.isEmpty());
        assertTrue(users.size() >= 1);
    }

    @Test
    @Order(4)
    @DisplayName("Should find user by email")
    void shouldFindByEmail() {
        // Arrange
        var found = repository.findByEmail("john.doe@example.com");

        // Assert
        assertNotNull(found);
        assertEquals("john.doe@example.com", found.getEmail());
    }

    @Test
    @Order(5)
    @DisplayName("Should delete a user")
    void shouldDeleteUser() {
        // Arrange
        var userToDelete = repository.get(1);

        // Act
        repository.delete(userToDelete);

        // Assert
        assertThrows(EntityNotFoundException.class, () -> repository.get(1));
    }
}