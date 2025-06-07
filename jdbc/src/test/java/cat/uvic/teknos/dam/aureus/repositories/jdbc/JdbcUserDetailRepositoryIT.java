package cat.uvic.teknos.dam.aureus.repositories.jdbc;

import cat.uvic.teknos.dam.aureus.UserDetail;
import cat.uvic.teknos.dam.aureus.impl.UserDetailImpl;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.DataSource;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.SingleConnectionDataSource;

import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JdbcUserDetailRepositoryIT {

    private DataSource dataSource;
    private JdbcUserDetailRepository repo;

    @BeforeAll
    void setupDatabase() throws Exception {
        String driver = "h2";
        String server = "mem";
        String database = "testdb;DB_CLOSE_DELAY=-1";
        String user = "sa";
        String password = "";
        var format = "jdbc:%s:%s:%s";

        dataSource = new SingleConnectionDataSource(format, driver, server, database, user, password);
        repo = new JdbcUserDetailRepository(dataSource);

        try (Connection con = dataSource.getConnection();
             Statement st = con.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS USER_DETAIL (" +
                    "USER_ID INT PRIMARY KEY, " +
                    "BIRTHDATE DATE, " +
                    "PHONE VARCHAR(50), " +
                    "GENDER VARCHAR(10), " +
                    "NATIONALITY VARCHAR(50)" +
                    ")");
        }
    }

    @AfterAll
    void tearDown() throws Exception {
        try (Connection con = dataSource.getConnection();
             Statement st = con.createStatement()) {
            st.execute("DROP TABLE USER_DETAIL");
        }
    }

    @Test
    void shouldSaveAndGetUserDetail() {
        UserDetail ud = new UserDetailImpl();
        ud.setId(1);
        ud.setBirthdate(LocalDate.of(1990, 1, 1));
        ud.setPhone("123456789");
        ud.setGender("M");
        ud.setNationality("Spanish");

        repo.save(ud);

        UserDetail fetched = repo.get(1);
        assertNotNull(fetched);
        assertEquals(1, fetched.getId());
        assertEquals(ud.getBirthdate(), fetched.getBirthdate());
        assertEquals("123456789", fetched.getPhone());
        assertEquals("M", fetched.getGender());
        assertEquals("Spanish", fetched.getNationality());
    }

    @Test
    void shouldUpdateUserDetail() {
        UserDetail ud = new UserDetailImpl();
        ud.setId(2);
        ud.setBirthdate(LocalDate.of(2000, 1, 1));
        ud.setPhone("111222333");
        ud.setGender("F");
        ud.setNationality("French");

        repo.save(ud);

        // Update fields
        ud.setPhone("999888777");
        ud.setNationality("Italian");
        repo.save(ud);

        UserDetail updated = repo.get(2);
        assertNotNull(updated);
        assertEquals("999888777", updated.getPhone());
        assertEquals("Italian", updated.getNationality());
    }

    @Test
    void shouldDeleteUserDetail() {
        UserDetail ud = new UserDetailImpl();
        ud.setId(3);
        ud.setPhone("555666777");

        repo.save(ud);

        UserDetail existing = repo.get(3);
        assertNotNull(existing);

        repo.delete(ud);

        UserDetail deleted = repo.get(3);
        assertNull(deleted);
    }

    @Test
    void shouldGetAllUserDetails() {
        // Clear table before inserting
        try (var con = dataSource.getConnection();
             var st = con.createStatement()) {
            st.execute("DELETE FROM USER_DETAIL");
        } catch (Exception e) {
            fail("Error clearing USER_DETAIL table: " + e.getMessage());
        }

        // Insert test data
        for (int i = 1; i <= 3; i++) {
            UserDetail ud = new UserDetailImpl();
            ud.setId(i);
            ud.setBirthdate(LocalDate.of(1990 + i, 1, 1));
            ud.setPhone("Phone" + i);
            ud.setGender(i % 2 == 0 ? "F" : "M");
            ud.setNationality(i % 2 == 0 ? "French" : "Spanish");

            repo.save(ud);
        }

        Set<UserDetail> all = repo.getAll();
        assertNotNull(all);
        assertEquals(3, all.size());
    }
}