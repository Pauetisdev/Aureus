import java.sql.DriverManager;
import java.sql.SQLException;

public class DataSourceSingleConnection implements DataSource {
    @Override
    public Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/m0486", "root", "rootpassword");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        return connection;
    }

    private Connection connection;
}