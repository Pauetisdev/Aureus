package cat.uvic.teknos.dam.aureus.model.jpa;

import cat.uvic.teknos.dam.aureus.model.jpa.repositories.JpaTransactionRepository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.EntityNotFoundException;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JpaTransactionTest {

    private static EntityManagerFactory entityManagerFactory;
    private JpaTransactionRepository repository;

    // IDs
    private static final int BUYER_ID = 1;
    private static final int SELLER_ID = 2;

    @BeforeAll
    static void setUp() {
        entityManagerFactory = Persistence.createEntityManagerFactory("aureus_test");
    }

    @BeforeEach
    void beforeEach() {
        this.repository = new JpaTransactionRepository(entityManagerFactory.createEntityManager());
    }

    @AfterAll
    static void tearDown() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Debe insertar una transacción y recuperarla")
    void shouldInsertAndGetTransaction() {
        // Arrange
        var buyer = createAndPersistUser(BUYER_ID);
        var seller = createAndPersistUser(SELLER_ID);

        var transaction = new JpaTransaction();
        transaction.setBuyer(buyer);
        transaction.setSeller(seller);
        transaction.setTransactionDate(LocalDateTime.now());

        // Act
        repository.save(transaction);

        // Assert
        var retrieved = repository.get(transaction.getId());
        assertNotNull(retrieved);
        assertEquals(BUYER_ID, retrieved.getBuyer().getId());
        assertEquals(SELLER_ID, retrieved.getSeller().getId());
        assertNotNull(retrieved.getTransactionDate());
    }

    @Test
    @Order(2)
    @DisplayName("Debe actualizar una transacción existente")
    void shouldUpdateTransaction() {
        // Arrange
        var transaction = repository.get(1); // Creado en el test anterior
        LocalDateTime oldDate = transaction.getTransactionDate();
        LocalDateTime newDate = LocalDateTime.now().plusHours(1);
        transaction.setTransactionDate(newDate);

        // Act
        repository.save(transaction);

        // Assert
        var updated = repository.get(1);
        assertNotNull(updated);
        assertTrue(updated.getTransactionDate().isAfter(oldDate));
    }

    @Test
    @Order(3)
    @DisplayName("Debe obtener todas las transacciones")
    void shouldGetAllTransactions() {
        // Act
        Set<JpaTransaction> transactions = repository.getAll();

        // Assert
        assertNotNull(transactions);
        assertFalse(transactions.isEmpty());
        assertTrue(transactions.size() >= 1);
    }

    @Test
    @Order(4)
    @DisplayName("Debe encontrar transacciones dentro de un rango de fechas")
    void shouldFindByDateRange() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(1);

        // Act
        List<JpaTransaction> result = repository.findByDateRange(start, end);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (var t : result) {
            assertTrue(t.getTransactionDate().isAfter(start.minusSeconds(1)));
            assertTrue(t.getTransactionDate().isBefore(end.plusSeconds(1)));
        }
    }

    @Test
    @Order(5)
    @DisplayName("Debe eliminar una transacción")
    void shouldDeleteTransaction() {
        // Arrange
        var transactionToDelete = repository.get(1);

        // Act
        repository.delete(transactionToDelete);

        // Assert
        assertThrows(EntityNotFoundException.class, () -> repository.get(1));
    }

    // --- Métodos auxiliares ---

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