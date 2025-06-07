package cat.uvic.teknos.dam.aureus.model.jpa;

import cat.uvic.teknos.dam.aureus.model.jpa.JpaUser;
import cat.uvic.teknos.dam.aureus.model.jpa.JpaUserDetail;
import cat.uvic.teknos.dam.aureus.model.jpa.repositories.JpaUserDetailRepository;
import cat.uvic.teknos.dam.aureus.model.jpa.repositories.JpaUserRepository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.EntityNotFoundException;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JpaUserDetailTest {

    private static EntityManagerFactory entityManagerFactory;
    private JpaUserDetailRepository detailRepository;
    private JpaUserRepository userRepository;
    private static Integer userId;

    @BeforeAll
    static void setUp() {
        entityManagerFactory = Persistence.createEntityManagerFactory("aureus_test");
    }

    @BeforeEach
    void beforeEach() {
        var entityManager = entityManagerFactory.createEntityManager();
        this.detailRepository = new JpaUserDetailRepository(entityManager);
        this.userRepository = new JpaUserRepository(entityManager);
    }

    @AfterAll
    static void tearDown() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Should insert and retrieve a user detail")
    void shouldInsertAndGetUserDetail() {
        // Crear y guardar usuario
        var user = new JpaUser();
        user.setUsername("john_doe");
        user.setEmail("john.doe@example.com");
        user.setPasswordHash("hash123");
        user.setJoinDate(LocalDateTime.now());
        userRepository.save(user);
        userId = user.getId();

        // Crear y asociar detalle
        var detail = new JpaUserDetail();
        detail.setUser(user);
        detail.setBirthdate(LocalDate.of(1990, 1, 1));
        detail.setGender("Male");
        detail.setNationality("Spanish");
        detail.setPhone("123456789");

        // Guardar y comprobar
        detailRepository.save(detail);

        var retrieved = detailRepository.get(detail.getId());
        assertNotNull(retrieved);
        assertEquals("Male", retrieved.getGender());
        assertEquals("Spanish", retrieved.getNationality());
        assertEquals("123456789", retrieved.getPhone());
    }

    @Test
    @Order(2)
    @DisplayName("Should update an existing user detail")
    void shouldUpdateUserDetail() {
        var detail = detailRepository.get(userId);
        detail.setNationality("French");
        detailRepository.save(detail);

        var updated = detailRepository.get(userId);
        assertNotNull(updated);
        assertEquals("French", updated.getNationality());
    }

    @Test
    @Order(3)
    @DisplayName("Should get all user details")
    void shouldGetAllUserDetails() {
        Set<JpaUserDetail> details = detailRepository.getAll();
        assertNotNull(details);
        assertFalse(details.isEmpty());
        assertTrue(details.size() >= 1);
    }

    @Test
    @Order(4)
    @DisplayName("Should delete a user detail")
    void shouldDeleteUserDetail() {
        var detailToDelete = detailRepository.get(userId);
        detailRepository.delete(detailToDelete);
        assertThrows(EntityNotFoundException.class, () -> detailRepository.get(userId));
    }
}