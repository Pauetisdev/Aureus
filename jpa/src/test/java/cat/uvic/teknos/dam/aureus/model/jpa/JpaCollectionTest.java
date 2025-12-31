package cat.uvic.teknos.dam.aureus.model.jpa;

import cat.uvic.teknos.dam.aureus.model.jpa.JpaCollection;
import cat.uvic.teknos.dam.aureus.model.jpa.JpaUser;
import cat.uvic.teknos.dam.aureus.model.jpa.repositories.JpaCollectionRepository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.EntityNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JpaCollectionTest {

    private static EntityManagerFactory entityManagerFactory;
    private static EntityManager entityManager;
    private static JpaCollectionRepository repository;
    private static int collectionId;
    private static final int USER_ID = 1;

    @BeforeAll
    static void setUp() {
        entityManagerFactory = Persistence.createEntityManagerFactory("aureus_test");
        entityManager = entityManagerFactory.createEntityManager();
        repository = new JpaCollectionRepository(entityManager);
    }

    @AfterAll
    static void tearDown() {
        if (entityManager != null) {
            entityManager.close();
        }
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }

    private JpaUser createAndPersistUser(int id) {
        var em = repository.getEntityManager();
        var tx = em.getTransaction();

        tx.begin();
        var user = new JpaUser();
        user.setUsername("test_user_" + id);
        user.setEmail("user" + id + "@example.com");
        user.setPasswordHash("hash123");
        em.persist(user);
        tx.commit();

        return user;
    }

    @Test
    @Order(1)
    void shouldInsertAndGetCollection() {
        var user = createAndPersistUser(USER_ID);

        var collection = new JpaCollection();
        collection.setCollectionName("Ancient Coins");
        collection.setDescription("A collection of rare ancient coins.");
        collection.setUser(user);

        repository.save(collection);
        collectionId = collection.getId();

        var retrieved = repository.get(collectionId);
        assertNotNull(retrieved);
        assertEquals("Ancient Coins", retrieved.getCollectionName());
        assertEquals("A collection of rare ancient coins.", retrieved.getDescription());
        assertEquals(user.getId(), retrieved.getUser().getId());
    }

    @Test
    @Order(2)
    void shouldUpdateCollection() {
        var collection = repository.get(collectionId);
        collection.setCollectionName("Updated Collection Name");
        repository.save(collection);

        var updated = repository.get(collectionId);
        assertNotNull(updated);
        assertEquals("Updated Collection Name", updated.getCollectionName());
    }

    @Test
    @Order(3)
    void shouldGetAllCollections() {
        List<JpaCollection> collections = repository.getAll();
        assertNotNull(collections);
        assertFalse(collections.isEmpty());
        assertTrue(collections.size() >= 1);
    }

    @Test
    @Order(4)
    void shouldDeleteCollection() {
        var collectionToDelete = repository.get(collectionId);
        repository.delete(collectionToDelete);
        assertThrows(EntityNotFoundException.class, () -> repository.get(collectionId));
    }
}