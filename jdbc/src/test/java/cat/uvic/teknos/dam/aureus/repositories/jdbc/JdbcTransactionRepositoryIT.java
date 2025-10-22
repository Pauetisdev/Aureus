package cat.uvic.teknos.dam.aureus.repositories.jdbc;

import cat.uvic.teknos.dam.aureus.Transaction;
import cat.uvic.teknos.dam.aureus.User;
import cat.uvic.teknos.dam.aureus.impl.TransactionImpl;
import cat.uvic.teknos.dam.aureus.repositories.UserRepository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.DataSource;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.SimpleDriverManagerDataSource;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JdbcTransactionRepositoryIT {

    private JdbcTransactionRepository transactionRepository;
    private UserRepository userRepository;

    @BeforeAll
    void setupDatabase() throws SQLException {
        String driver = "h2";
        String server = "mem";
        String database = "testdb;DB_CLOSE_DELAY=-1";
        String user = "sa";
        String password = "";
        var format = "jdbc:%s:%s:%s";

        DataSource dataSource = new SimpleDriverManagerDataSource(
                String.format(format, driver, server, database), user, password
        );

        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            // Ensure clean state: drop dependent tables first to avoid FK constraint errors
            st.execute("DROP TABLE IF EXISTS COIN_TRANSACTION");
            st.execute("DROP TABLE IF EXISTS COIN_COLLECTION");
            st.execute("DROP TABLE IF EXISTS COIN");
            st.execute("DROP TABLE IF EXISTS COLLECTION");
            st.execute("DROP TABLE IF EXISTS USER_DETAIL");
            st.execute("DROP TABLE IF EXISTS \"TRANSACTION\"");
            st.execute("DROP TABLE IF EXISTS \"USER\"");

            st.execute("CREATE TABLE \"USER\" (" +
                    "USER_ID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "USERNAME VARCHAR(255), " +
                    "EMAIL VARCHAR(255), " +
                    "PASSWORD_HASH VARCHAR(255), " +
                    "JOIN_DATE TIMESTAMP)");

            st.execute("CREATE TABLE \"TRANSACTION\" (" +
                    "TRANSACTION_ID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "TRANSACTION_DATE TIMESTAMP, " +
                    "BUYER_ID INT, " +
                    "SELLER_ID INT, " +
                    "FOREIGN KEY (BUYER_ID) REFERENCES \"USER\"(USER_ID), " +
                    "FOREIGN KEY (SELLER_ID) REFERENCES \"USER\"(USER_ID))");

            st.execute("INSERT INTO \"USER\" (USERNAME, EMAIL, PASSWORD_HASH) VALUES ('buyer', 'buyer@example.com', 'pass')");
            st.execute("INSERT INTO \"USER\" (USERNAME, EMAIL, PASSWORD_HASH) VALUES ('seller', 'seller@example.com', 'pass')");
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
        tx.setBuyer(buyer);
        tx.setSeller(seller);

        transactionRepository.save(tx);

        List<Transaction> allTransactions = transactionRepository.getAll();
        assertFalse(allTransactions.isEmpty(), "Debe haber al menos una transacción guardada");

        Transaction fetched = allTransactions.get(0);
        assertEquals(1, fetched.getBuyer().getId());
        assertEquals(2, fetched.getSeller().getId());
    }
}
