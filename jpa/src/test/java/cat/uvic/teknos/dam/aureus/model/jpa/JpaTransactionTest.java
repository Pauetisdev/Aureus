package cat.uvic.teknos.dam.aureus.model.jpa;

import cat.uvic.teknos.dam.aureus.model.jpa.repositories.JpaTransactionRepository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.EntityNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.List;



import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JpaTransactionTest {

    private static EntityManagerFactory entityManagerFactory;
    private EntityManager em;
    private JpaTransactionRepository repository;

    @BeforeAll
    static void setUp() {
        entityManagerFactory = Persistence.createEntityManagerFactory("aureus_test");
    }

    @BeforeEach
    void beforeEach() {
        em = entityManagerFactory.createEntityManager();
        repository = new JpaTransactionRepository(em);
    }

    @AfterEach
    void afterEach() {
        if (em != null && em.isOpen()) {
            em.close();
        }
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
        var buyer = createAndPersistUser(em, "buyer");
        var seller = createAndPersistUser(em, "seller");

        var transaction = new JpaTransaction();
        transaction.setBuyer(buyer);
        transaction.setSeller(seller);
        transaction.setTransactionDate(LocalDateTime.now());

        repository.save(transaction);

        var retrieved = repository.get(transaction.getId());
        assertNotNull(retrieved);
        assertEquals(buyer.getId(), retrieved.getBuyer().getId());
        assertEquals(seller.getId(), retrieved.getSeller().getId());
        assertNotNull(retrieved.getTransactionDate());
    }

    @Test
    @Order(2)
    @DisplayName("Debe actualizar una transacción existente")
    void shouldUpdateTransaction() {
        var transaction = repository.get(1);
        LocalDateTime oldDate = transaction.getTransactionDate();
        LocalDateTime newDate = LocalDateTime.now().plusHours(1);
        transaction.setTransactionDate(newDate);

        repository.save(transaction);

        var updated = repository.get(1);
        assertNotNull(updated);
        assertTrue(updated.getTransactionDate().isAfter(oldDate));
    }

    @Test
    @Order(3)
    @DisplayName("Debe obtener todas las transacciones")
    void shouldGetAllTransactions() {
        List<JpaTransaction> transactions = repository.getAll();

        assertNotNull(transactions);
        assertFalse(transactions.isEmpty());
        assertTrue(transactions.size() >= 1);
    }

    @Test
    @Order(4)
    @DisplayName("Debe encontrar transacciones dentro de un rango de fechas")
    void shouldFindByDateRange() {
        // Crear y persistir una transacción dentro del rango para hacer la prueba independiente
        var buyer = createAndPersistUser(em, "buyer_range");
        var seller = createAndPersistUser(em, "seller_range");
        var txToFind = new JpaTransaction();
        LocalDateTime txDate = LocalDateTime.now();
        txToFind.setBuyer(buyer);
        txToFind.setSeller(seller);
        txToFind.setTransactionDate(txDate);
        repository.save(txToFind);

        LocalDateTime start = txDate.minusHours(1);
        LocalDateTime end = txDate.plusHours(1);

        List<JpaTransaction> result = repository.findByDateRange(start, end);

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
        var transactionToDelete = repository.get(1);

        repository.delete(transactionToDelete);

        assertThrows(EntityNotFoundException.class, () -> repository.get(1));
    }

    // --- Métodos auxiliares ---

    private JpaUser createAndPersistUser(EntityManager em, String username) {
        var tx = em.getTransaction();
        tx.begin();
        var user = new JpaUser();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash("hash123");
        em.persist(user);
        tx.commit();
        return user;
    }
}