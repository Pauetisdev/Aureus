package cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions;

import java.sql.SQLException;

public class CrudException extends RuntimeException {
    public CrudException(String message, SQLException e) {
        super(message);
    }

    public CrudException(SQLException e) {
    }
}
