package cat.uvic.teknos.dam.aureus.repositories.jdbc;

import cat.uvic.teknos.dam.aureus.UserDetail;
import cat.uvic.teknos.dam.aureus.impl.UserDetailImpl;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.DataSource;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.SingleConnectionDataSource;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JdbcUserDetailRepositoryIT {

    DataSource dataSource;
    JdbcUserDetailRepository repo;

    @BeforeAll
    void setupDatabase() throws Exception {
        String driver = "org.h2.Driver";
        String server = "jdbc:h2:mem:";
        String database = "testdb;DB_CLOSE_DELAY=-1";
        String user = "sa";
        String password = "";

        dataSource = new SingleConnectionDataSource(driver, server, database, user, password);
        repo = new JdbcUserDetailRepository(dataSource);

        try (Connection con = dataSource.getConnection();
             Statement st = con.createStatement()) {

            st.execute("CREATE TABLE USER_DETAIL (" +
                    "USER_ID INT PRIMARY KEY, " +
                    "BIRTHDATE DATE, " +
                    "PHONE VARCHAR(50), " +
                    "GENDER VARCHAR(10), " +
                    "NATIONALITY VARCHAR(50)" +
                    ")");

            // Opcional: insertamos datos iniciales si quieres
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
    void testSaveAndGet() {
        UserDetail ud = new UserDetailImpl();
        ud.setId(1);
        ud.setBirthday((java.sql.Date) new Date(631152000000L)); // 1 Jan 1990
        ud.setPhone("123456789");
        ud.setGender("M");
        ud.setNationality("Española");

        repo.save(ud);

        UserDetail fetched = repo.get(1);
        assertNotNull(fetched);
        assertEquals(1, fetched.getId());
        assertEquals(ud.getBirthday(), fetched.getBirthday());
        assertEquals("123456789", fetched.getPhone());
        assertEquals("M", fetched.getGender());
        assertEquals("Española", fetched.getNationality());
    }

    @Test
    void testUpdate() {
        UserDetail ud = new UserDetailImpl();
        ud.setId(2);
        ud.setBirthday((java.sql.Date) new Date(946684800000L)); // 1 Jan 2000
        ud.setPhone("111222333");
        ud.setGender("F");
        ud.setNationality("Francesa");

        repo.save(ud);

        // Modificar y actualizar
        ud.setPhone("999888777");
        ud.setNationality("Italiana");
        repo.save(ud);

        UserDetail updated = repo.get(2);
        assertNotNull(updated);
        assertEquals("999888777", updated.getPhone());
        assertEquals("Italiana", updated.getNationality());
    }

    @Test
    void testDelete() {
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
    void testGetAll() {
        // Limpiar tabla antes
        try (var con = dataSource.getConnection();
             var st = con.createStatement()) {
            st.execute("DELETE FROM USER_DETAIL");
        } catch (Exception e) {
            fail("Error limpiando tabla USER_DETAIL: " + e.getMessage());
        }

        // Insertamos varios registros
        for (int i = 1; i <= 3; i++) {
            UserDetail ud = new UserDetailImpl();
            ud.setId(i);
            ud.setPhone("Phone " + i);
            repo.save(ud);
        }

        Set<UserDetail> all = repo.getAll();
        assertEquals(3, all.size());
    }
}
