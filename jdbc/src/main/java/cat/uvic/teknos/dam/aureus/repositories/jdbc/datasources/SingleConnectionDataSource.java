package cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.DataSourceException;

public class SingleConnectionDataSource implements DataSource, AutoCloseable {
    private final String format;
    private Connection connection;
    private final String driver;
    private final String server;
    private final String database;
    private final String user;
    private final String password;

    public SingleConnectionDataSource(String format, String driver, String server, String database, String user, String password) {
        this.format = format;
        this.driver = driver;
        this.server = server;
        this.database = database;
        this.user = user;
        this.password = password;
    }

    public SingleConnectionDataSource(String driver, String server, String database, String user, String password) {
        this.format = "jdbc:%s:%s/%s";
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

        format = properties.getProperty("format");
        driver = properties.getProperty("driver");
        server = properties.getProperty("server");
        database = properties.getProperty("database");
        user = properties.getProperty("user");
        password = properties.getProperty("password");
    }

    @Override
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                var url = String.format(format, driver, server, database);
                connection = DriverManager.getConnection(url, user, password);
            }
        } catch (SQLException e) {
            throw new DataSourceException("Failed to get database connection", e);
        }
        return connection;
    }

    @Override
    public void close() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            connection = null;
        }
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