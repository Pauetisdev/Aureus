package cat.uvic.teknos.dam.aureus.model.jpa;

import jakarta.persistence.*;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class JpaCoinTransactionTest {

    private static EntityManagerFactory emf;
    private EntityManager em;

    private static final int COIN_ID = 1;
    private static final int TRANSACTION_ID = 1;
    private static final int BUYER_ID = 1;
    private static final int SELLER_ID = 2;

    @BeforeAll
    static void setUpClass() {
        emf = Persistence.createEntityManagerFactory("aureus_test");
    }

    @AfterAll
    static void tearDownClass() {
        if (emf != null) {
            emf.close();
        }
    }

    @BeforeEach
    void setUp() {
        em = emf.createEntityManager();
    }

    @AfterEach
    void tearDown() {
        if (em != null) {
            em.close();
        }
    }

    private JpaUser createAndPersistUser() {
        JpaUser user = new JpaUser();
        user.setUsername("user");
        user.setPasswordHash("pass");
        em.getTransaction().begin();
        em.persist(user);
        em.getTransaction().commit();
        return user;
    }

    private JpaCoin createAndPersistCoin() {
        JpaCoin coin = new JpaCoin();
        coin.setCoinName("Golden Dollar");
        coin.setCoinYear(2020);
        coin.setCoinMaterial("Gold");
        coin.setCoinWeight(BigDecimal.valueOf(10.0));
        coin.setCoinDiameter(BigDecimal.valueOf(25.0));
        coin.setEstimatedValue(BigDecimal.valueOf(100.0));
        coin.setOriginCountry("Spain");
        em.getTransaction().begin();
        em.persist(coin);
        em.getTransaction().commit();
        return coin;
    }

    private JpaTransaction createAndPersistTransaction(JpaUser buyer, JpaUser seller) {
        JpaTransaction transaction = new JpaTransaction();
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setBuyer(buyer);
        transaction.setSeller(seller);
        em.getTransaction().begin();
        em.persist(transaction);
        em.getTransaction().commit();
        return transaction;
    }

    @Test
    void testPersistCoinTransaction() {
        // Crear y persistir usuarios con todos los campos obligatorios
        JpaUser buyer = new JpaUser();
        buyer.setUsername("comprador");
        buyer.setPasswordHash("pass1");
        buyer.setEmail("comprador@email.com"); // Campo obligatorio
        em.getTransaction().begin();
        em.persist(buyer);
        em.getTransaction().commit();

        JpaUser seller = new JpaUser();
        seller.setUsername("vendedor");
        seller.setPasswordHash("pass2");
        seller.setEmail("vendedor@email.com"); // Campo obligatorio
        em.getTransaction().begin();
        em.persist(seller);
        em.getTransaction().commit();

        // Crear y persistir moneda
        JpaCoin coin = new JpaCoin();
        coin.setCoinName("Golden Dollar");
        coin.setCoinYear(2020);
        coin.setCoinMaterial("Gold");
        coin.setCoinWeight(new BigDecimal("10.0"));
        coin.setCoinDiameter(new BigDecimal("25.0"));
        coin.setEstimatedValue(new BigDecimal("100.0"));
        coin.setOriginCountry("Spain");
        em.getTransaction().begin();
        em.persist(coin);
        em.getTransaction().commit();

        // Crear y persistir transacción
        JpaTransaction transaction = new JpaTransaction();
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setBuyer(buyer);
        transaction.setSeller(seller);
        em.getTransaction().begin();
        em.persist(transaction);
        em.getTransaction().commit();

        // Crear y persistir CoinTransaction con ID compuesto
        JpaCoinTransaction coinTransaction = new JpaCoinTransaction();
        coinTransaction.setCoin(coin);
        coinTransaction.setTransaction(transaction);
        coinTransaction.setId(new CoinTransactionId(coin.getId(), transaction.getId()));
        coinTransaction.setTransactionPrice(new BigDecimal("120.00"));
        coinTransaction.setCurrency("EUR");

        em.getTransaction().begin();
        em.persist(coinTransaction);
        em.getTransaction().commit();

        // Verificar que se ha persistido correctamente
        assertNotNull(coinTransaction.getId());
        JpaCoinTransaction found = em.find(JpaCoinTransaction.class, coinTransaction.getId());
        assertNotNull(found);
        assertEquals(coin.getId(), found.getCoin().getId());
        assertEquals(transaction.getId(), found.getTransaction().getId());
    }
}