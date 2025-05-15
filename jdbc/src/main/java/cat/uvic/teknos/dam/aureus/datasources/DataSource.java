package cat.uvic.teknos.dam.aureus.datasources;

import java.sql.Connection;

public interface DataSource {
    Connection getConnection();
}
