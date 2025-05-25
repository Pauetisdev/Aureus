package cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources;

import java.sql.Connection;

public interface DataSource {
    Connection getConnection();
}
