package cat.uvic.teknos.dam.aureus.model.jpa;

import cat.uvic.teknos.dam.aureus.model.jpa.repositories.JpaCollectionRepository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.EntityNotFoundException;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JpaCollectionTest {

    private static EntityManagerFactory entityManagerFactory;
    private JpaCollectionRepository repository;

    // Test user ID to use in tests
    private static final int USER_ID = 1;

    @BeforeAll
    static void setUp() {
        entityManagerFactory = Persistence.createEntityManagerFactory("aureus_test");
    }

    @BeforeEach
    void beforeEach() {
        this.repository = new JpaCollectionRepository(entityManagerFactory.createEntityManager());
    }

    @AfterAll
    static void tearDown() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Should insert a new collection and retrieve it")
    void shouldInsertAndGetCollection() {
        // Arrange
        var user = createAndPersistUser(USER_ID);

        var collection = new JpaCollection();
        collection.setCollectionName("Ancient Coins");
        collection.setDescription("A collection of rare ancient coins.");
        collection.setUser(user);

        // Act
        repository.save(collection);

        // Assert
        var retrieved = repository.get(collection.getId());
        assertNotNull(retrieved);
        assertEquals("Ancient Coins", retrieved.getCollectionName());
        assertEquals("A collection of rare ancient coins.", retrieved.getDescription());
        assertEquals(USER_ID, retrieved.getUser().getId());
    }

    @Test
    @Order(2)
    @DisplayName("Should update an existing collection")
    void shouldUpdateCollection() {
        // Arrange
        var collection = repository.get(1); // Created in previous test
        collection.setCollectionName("Updated Collection Name");

        // Act
        repository.save(collection);

        // Assert
        var updated = repository.get(1);
        assertNotNull(updated);
        assertEquals("Updated Collection Name", updated.getCollectionName());
    }

    @Test
    @Order(3)
    @DisplayName("Should get all collections")
    void shouldGetAllCollections() {
        // Act
        Set<JpaCollection> collections = repository.getAll();

        // Assert
        assertNotNull(collections);
        assertFalse(collections.isEmpty());
        assertTrue(collections.size() >= 1);
    }

    @Test
    @Order(4)
    @DisplayName("Should delete a collection")
    void shouldDeleteCollection() {
        // Arrange
        var collectionToDelete = repository.get(1);

        // Act
        repository.delete(collectionToDelete);

        // Assert
        assertThrows(EntityNotFoundException.class, () -> repository.get(1));
    }

    // --- Helpers ---

    private JpaUser createAndPersistUser(int id) {
        var em = entityManagerFactory.createEntityManager();
        var tx = em.getTransaction();

        tx.begin();
        var user = new JpaUser();
        user.setId(id);
        user.setUsername("test_user_" + id);
        user.setEmail("user" + id + "@example.com");
        user.setPasswordHash("hash123");
        em.persist(user);
        tx.commit();
        em.close();

        return user;
    }
}