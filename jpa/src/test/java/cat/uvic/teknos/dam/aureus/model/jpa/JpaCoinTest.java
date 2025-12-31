package cat.uvic.teknos.dam.aureus.model.jpa;

import cat.uvic.teknos.dam.aureus.model.jpa.repositories.JpaCoinRepository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.EntityNotFoundException;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
class JpaCoinTest {

    private static EntityManagerFactory entityManagerFactory;
    private JpaCoinRepository repository;

    @BeforeAll
    static void setUp() {
        entityManagerFactory = Persistence.createEntityManagerFactory("aureus_test");
    }

    @BeforeEach
    void beforeEach() {
        this.repository = new JpaCoinRepository(entityManagerFactory.createEntityManager());
    }

    @AfterAll
    static void tearDown() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Should insert a new coin and retrieve it")
    void shouldInsertAndGetCoin() {
        // Arrange
        var coin = new JpaCoin();
        coin.setCoinName("Euro");
        coin.setCoinYear(2020);
        coin.setCoinMaterial("Silver");
        coin.setCoinWeight(BigDecimal.valueOf(5.0));
        coin.setCoinDiameter(BigDecimal.valueOf(20.0));
        coin.setEstimatedValue(BigDecimal.valueOf(100.0));
        coin.setOriginCountry("Spain");

        // Create and persist a minimal collection because JpaCoin.collection is not nullable
        var em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        var collection = new JpaCollection();
        collection.setCollectionName("Default Collection");
        em.persist(collection);
        em.getTransaction().commit();
        em.close();

        coin.setCollection(collection);

        // Act
        repository.save(coin);

        // Assert
        var retrieved = repository.get(coin.getId());
        assertNotNull(retrieved);
        assertEquals("Euro", retrieved.getCoinName());
        assertEquals("Spain", retrieved.getOriginCountry());
    }

    @Test
    @Order(2)
    @DisplayName("Should update an existing coin")
    void shouldUpdateCoin() {
        // Arrange
        var coin = repository.get(1); // Coin inserted in previous test
        coin.setCoinName("Updated Euro");

        // Act
        repository.save(coin);

        // Assert
        var updated = repository.get(1);
        assertNotNull(updated);
        assertEquals("Updated Euro", updated.getCoinName());
    }

    @Test
    @Order(3)
    @DisplayName("Should get all coins")
    void shouldGetAllCoins() {
        // Act
        List<JpaCoin> coins = repository.getAll();

        // Assert
        assertNotNull(coins);
        assertFalse(coins.isEmpty());
        assertTrue(coins.size() >= 1);
    }

    @Test
    @Order(4)
    @DisplayName("Should find coins by material")
    void shouldFindByMaterial() {
        // Act
        List<JpaCoin> coins = repository.findByMaterial("Silver");

        // Assert
        assertNotNull(coins);
        assertFalse(coins.isEmpty());
        for (var c : coins) {
            assertEquals("Silver", c.getCoinMaterial());
        }
    }

    @Test
    @Order(5)
    @DisplayName("Should find coins by year")
    void shouldFindByYear() {
        // Act
        List<JpaCoin> coins = repository.findByYear(2020);

        // Assert
        assertNotNull(coins);
        assertFalse(coins.isEmpty());
        for (var c : coins) {
            assertEquals(2020, c.getCoinYear().intValue());
        }
    }

    @Test
    @Order(6)
    @DisplayName("Should delete a coin")
    void shouldDeleteCoin() {
        // Arrange
        var coinToDelete = repository.get(1);

        // Act
        repository.delete(coinToDelete);

        // Assert
        assertThrows(EntityNotFoundException.class, () -> repository.get(1));
    }
}