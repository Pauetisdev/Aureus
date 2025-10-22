package cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;

import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.DataSourceException;

/**
 * DataSource de pruebas que mantiene una única conexión real pero devuelve un proxy
 * cuya close() es no-op para que los tests puedan usar try-with-resources sin cerrar
 * la conexión subyacente.
 */
public class TestSingleConnectionDataSource implements DataSource, AutoCloseable {
    private final String format;
    private Connection connection; // real underlying connection
    private Connection connectionProxy;
    private final String driver;
    private final String server;
    private final String database;
    private final String user;
    private final String password;
    private final String dbInstanceSuffix; // unique suffix per instance to isolate in-memory DBs

    public TestSingleConnectionDataSource(String format, String driver, String server, String database, String user, String password) {
        this.format = format;
        this.driver = driver;
        this.server = server;
        this.database = database;
        this.user = user;
        this.password = password;
        this.dbInstanceSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    public TestSingleConnectionDataSource() {
        this.format = "jdbc:%s:%s:%s";
        this.driver = "h2";
        this.server = "mem";
        this.database = "testdb;DB_CLOSE_DELAY=-1";
        this.user = "sa";
        this.password = "";
        this.dbInstanceSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    @Override
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // If the provided database parameter contains extras (like ;DB_CLOSE_DELAY=...),
                // keep those extras but append a unique suffix to the base database name so each
                // TestSingleConnectionDataSource instance gets an isolated in-memory database.
                String db = database;
                String extras = "";
                if (database != null && database.contains(";")) {
                    int idx = database.indexOf(';');
                    db = database.substring(0, idx);
                    extras = database.substring(idx);
                }
                String uniqueDb = db + "_" + dbInstanceSuffix + extras;

                var url = String.format(format, driver, server, uniqueDb);
                connection = DriverManager.getConnection(url, user, password);
                connectionProxy = (Connection) Proxy.newProxyInstance(
                        Connection.class.getClassLoader(),
                        new Class[]{Connection.class},
                        new InvocationHandler() {
                            @Override
                            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                if ("close".equals(method.getName())) {
                                    // no-op so tests can use try-with-resources without closing the shared connection
                                    return null;
                                }
                                return method.invoke(connection, args);
                            }
                        }
                );
            }
        } catch (SQLException e) {
            throw new DataSourceException("Failed to get database connection", e);
        }
        return connectionProxy;
    }

    @Override
    public void close() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            connection = null;
            connectionProxy = null;
        }
    }
}
