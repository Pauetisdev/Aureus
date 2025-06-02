package cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions;

public class DataSourceException extends RuntimeException {
    public DataSourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
