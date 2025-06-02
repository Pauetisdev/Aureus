package cat.uvic.teknos.dam.aureus.model.jpa;

import cat.uvic.teknos.dam.aureus.model.jpa.repositories.JpaCoinTransactionRepository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.EntityNotFoundException;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
class JpaCoinTransactionTest {

    private static EntityManagerFactory entityManagerFactory;
    private JpaCoinTransactionRepository repository;

    // IDs
    private static final int COIN_ID = 1;
    private static final int TRANSACTION_ID = 1;

    @BeforeAll
    static void setUp() {
        entityManagerFactory = Persistence.createEntityManagerFactory("aureus_test");
    }

    @BeforeEach
    void beforeEach() {
        this.repository = new JpaCoinTransactionRepository(entityManagerFactory.createEntityManager());
    }

    @AfterAll
    static void tearDown() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Should insert and retrieve a CoinTransaction")
    void shouldInsertAndGetCoinTransaction() {
        // Arrange
        var coin = createAndPersistCoin();
        var transaction = createAndPersistTransaction();

        var id = new CoinTransactionId();
        id.setCoinId(COIN_ID);
        id.setTransactionId(TRANSACTION_ID);

        var coinTransaction = new JpaCoinTransaction();
        coinTransaction.setId(id);
        coinTransaction.setCoin(coin);
        coinTransaction.setTransaction(transaction);
        coinTransaction.setTransactionPrice(BigDecimal.valueOf(50.0));
        coinTransaction.setCurrency("EUR");

        // Act
        repository.save(coinTransaction);

        // Assert
        var retrieved = repository.get(id);
        assertNotNull(retrieved);
        assertEquals("EUR", retrieved.getCurrency());
        assertEquals(BigDecimal.valueOf(50.0), retrieved.getTransactionPrice());
        assertEquals(COIN_ID, retrieved.getId().getCoinId());
        assertEquals(TRANSACTION_ID, retrieved.getId().getTransactionId());
    }

    @Test
    @Order(2)
    @DisplayName("Should update an existing CoinTransaction")
    void shouldUpdateCoinTransaction() {
        // Arrange
        var id = new CoinTransactionId();
        id.setCoinId(COIN_ID);
        id.setTransactionId(TRANSACTION_ID);

        var existing = repository.get(id);
        existing.setTransactionPrice(BigDecimal.valueOf(75.0));
        existing.setCurrency("USD");

        // Act
        repository.save(existing);

        // Assert
        var updated = repository.get(id);
        assertEquals(BigDecimal.valueOf(75.0), updated.getTransactionPrice());
        assertEquals("USD", updated.getCurrency());
    }

    @Test
    @Order(3)
    @DisplayName("Should get all CoinTransactions")
    void shouldGetAllCoinTransactions() {
        // Act
        Set<JpaCoinTransaction> transactions = repository.getAll();

        // Assert
        assertNotNull(transactions);
        assertFalse(transactions.isEmpty());
    }

    @Test
    @Order(4)
    @DisplayName("Should delete a CoinTransaction")
    void shouldDeleteCoinTransaction() {
        // Arrange
        var id = new CoinTransactionId();
        id.setCoinId(COIN_ID);
        id.setTransactionId(TRANSACTION_ID);

        var toDelete = repository.get(id);

        // Act
        repository.delete(toDelete);

        // Assert
        assertThrows(EntityNotFoundException.class, () -> repository.get(id));
    }

    // --- Helpers ---

    private JpaCoin createAndPersistCoin() {
        var em = entityManagerFactory.createEntityManager();
        var tx = em.getTransaction();

        tx.begin();
        var coin = new JpaCoin();
        coin.setId(COIN_ID);
        coin.setCoinName("Golden Dollar");
        coin.setCoinYear(2020);
        coin.setCoinMaterial("Gold");
        coin.setCoinWeight(BigDecimal.valueOf(10.0));
        coin.setCoinDiameter(BigDecimal.valueOf(25.0));
        coin.setEstimatedValue(BigDecimal.valueOf(100.0));
        coin.setOriginCountry("Spain");

        em.persist(coin);
        tx.commit();
        em.close();

        return coin;
    }

    private JpaTransaction createAndPersistTransaction() {
        var em = entityManagerFactory.createEntityManager();
        var tx = em.getTransaction();

        tx.begin();
        var transaction = new JpaTransaction();
        transaction.setId(TRANSACTION_ID);
        transaction.setTransactionDate(LocalDateTime.now());

        // Buyers and sellers
        var buyer = new JpaUser();
        buyer.setId(1);
        var seller = new JpaUser();
        seller.setId(2);

        transaction.setBuyer(buyer);
        transaction.setSeller(seller);

        em.persist(transaction);
        tx.commit();
        em.close();

        return transaction;
    }
}