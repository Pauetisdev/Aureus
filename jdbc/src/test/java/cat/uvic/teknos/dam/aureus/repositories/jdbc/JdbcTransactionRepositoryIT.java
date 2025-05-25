package cat.uvic.teknos.dam.aureus.repositories.jdbc;

import cat.uvic.teknos.dam.aureus.Transaction;
import cat.uvic.teknos.dam.aureus.User;
import cat.uvic.teknos.dam.aureus.impl.TransactionImpl;
import cat.uvic.teknos.dam.aureus.impl.UserImpl;
import cat.uvic.teknos.dam.aureus.repositories.UserRepository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.DataSource;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.SingleConnectionDataSource;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JdbcTransactionRepositoryIT {

    private DataSource dataSource;
    private JdbcTransactionRepository transactionRepository;
    private UserRepository userRepository;

    @BeforeAll
    void setupDatabase() throws SQLException {
        // Parámetros de conexión H2 en memoria
        String driver = "org.h2.Driver";
        String server = "jdbc:h2:mem:";
        String database = "testdb;DB_CLOSE_DELAY=-1";
        String user = "sa";
        String password = "";

        // Inicializamos SingleConnectionDataSource
        dataSource = new SingleConnectionDataSource(driver, server, database, user, password);

        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            // Crear tabla USER con columnas reales
            st.execute("CREATE TABLE USER (" +
                    "USER_ID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "USERNAME VARCHAR(255), " +
                    "EMAIL VARCHAR(255), " +
                    "PASSWORD VARCHAR(255))");

            // Crear tabla TRANSACTION con claves foráneas
            st.execute("CREATE TABLE TRANSACTION (" +
                    "TRANSACTION_ID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "TRANSACTION_DATE TIMESTAMP, " +
                    "MATERIAL_NAME VARCHAR(255), " +
                    "BUYER_ID INT, " +
                    "SELLER_ID INT, " +
                    "FOREIGN KEY (BUYER_ID) REFERENCES USER(USER_ID), " +
                    "FOREIGN KEY (SELLER_ID) REFERENCES USER(USER_ID))");

            // Insertar usuarios
            st.execute("INSERT INTO USER (USERNAME, EMAIL, PASSWORD) VALUES ('buyer', 'buyer@example.com', 'pass')");
            st.execute("INSERT INTO USER (USERNAME, EMAIL, PASSWORD) VALUES ('seller', 'seller@example.com', 'pass')");
        }

        userRepository = new JdbcUserRepository(dataSource);
        transactionRepository = new JdbcTransactionRepository(dataSource, userRepository);
    }

    @Test
    void testSaveAndGetTransaction() {
        User buyer = userRepository.get(1);
        User seller = userRepository.get(2);

        Transaction tx = new TransactionImpl();
        tx.setTransactionDate(new Timestamp(System.currentTimeMillis()));
        tx.setMaterialName("Aureus");
        tx.setBuyer(buyer);
        tx.setSeller(seller);

        transactionRepository.save(tx);

        Set<Transaction> allTransactions = transactionRepository.getAll();
        assertFalse(allTransactions.isEmpty(), "Debe haber al menos una transacción guardada");

        Transaction fetched = allTransactions.iterator().next();
        assertEquals("Aureus", fetched.getMaterialName());
        assertEquals(1, fetched.getBuyer().getId());
        assertEquals(2, fetched.getSeller().getId());
    }
}
