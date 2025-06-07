package cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SimpleDriverManagerDataSource implements DataSource {
    private final String url;
    private final String user;
    private final String password;

    public SimpleDriverManagerDataSource(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}