package cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources;

import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.DataSourceException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class SingleConnectionDataSource implements DataSource {
    private Connection connection;
    private final String driver;
    private final String server;
    private final String database;
    private final String user;
    private final String password;

    public SingleConnectionDataSource(String driver, String server, String database, String user, String password) {
        this.driver = driver;
        this.server = server;
        this.database = database;
        this.user = user;
        this.password = password;
    }

    public SingleConnectionDataSource() {
        var properties = new Properties();
        try {
            properties.load(this.getClass().getResourceAsStream("/datasource.properties"));
        } catch (IOException e) {
            throw new DataSourceException("Failed to load datasource.properties", e);
        }

        driver = properties.getProperty("driver");
        server = properties.getProperty("server");
        database = properties.getProperty("database");
        user = properties.getProperty("user");
        password = properties.getProperty("password");
    }

    @Override
    public Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName(driver);
                connection = DriverManager.getConnection(
                        String.format("jdbc:%s://%s/%s", driver, server, database),
                        user,
                        password
                );
            } catch (SQLException | ClassNotFoundException e) {
                throw new DataSourceException("Failed to get database connection", e);
            }
        }
        return connection;
    }

    public String getDriver() {
        return driver;
    }

    public String getServer() {
        return server;
    }

    public String getDatabase() {
        return database;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
