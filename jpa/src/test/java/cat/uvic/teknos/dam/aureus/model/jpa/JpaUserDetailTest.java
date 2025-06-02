package cat.uvic.teknos.dam.aureus.model.jpa;

import cat.uvic.teknos.dam.aureus.model.jpa.JpaUserDetail;
import cat.uvic.teknos.dam.aureus.model.jpa.repositories.JpaUserDetailRepository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.EntityNotFoundException;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JpaUserDetailTest {

    private static EntityManagerFactory entityManagerFactory;
    private JpaUserDetailRepository repository;

    @BeforeAll
    static void setUp() {
        entityManagerFactory = Persistence.createEntityManagerFactory("aureus_test");
    }

    @BeforeEach
    void beforeEach() {
        this.repository = new JpaUserDetailRepository(entityManagerFactory.createEntityManager());
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
        // Arrange
        var detail = new JpaUserDetail();
        detail.setId(1); // Assuming a user with ID 1 exists
        detail.setBirthdate(LocalDate.of(1990, 1, 1));
        detail.setGender("Male");
        detail.setNationality("Spanish");
        detail.setPhone("123456789");

        // Act
        repository.save(detail);

        // Assert
        var retrieved = repository.get(detail.getId());
        assertNotNull(retrieved);
        assertEquals("Male", retrieved.getGender());
        assertEquals("Spanish", retrieved.getNationality());
        assertEquals("123456789", retrieved.getPhone());
    }

    @Test
    @Order(2)
    @DisplayName("Should update an existing user detail")
    void shouldUpdateUserDetail() {
        // Arrange
        var detail = repository.get(1);
        detail.setNationality("French");

        // Act
        repository.save(detail);

        // Assert
        var updated = repository.get(1);
        assertNotNull(updated);
        assertEquals("French", updated.getNationality());
    }

    @Test
    @Order(3)
    @DisplayName("Should get all user details")
    void shouldGetAllUserDetails() {
        // Act
        Set<JpaUserDetail> details = repository.getAll();

        // Assert
        assertNotNull(details);
        assertFalse(details.isEmpty());
        assertTrue(details.size() >= 1);
    }

    @Test
    @Order(4)
    @DisplayName("Should delete a user detail")
    void shouldDeleteUserDetail() {
        // Arrange
        var detailToDelete = repository.get(1);

        // Act
        repository.delete(detailToDelete);

        // Assert
        assertThrows(EntityNotFoundException.class, () -> repository.get(1));
    }
}