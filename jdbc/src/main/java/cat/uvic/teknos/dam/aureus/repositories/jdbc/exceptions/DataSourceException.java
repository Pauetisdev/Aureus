package cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions;

public class DataSourceException extends RuntimeException {
    public DataSourceException(String message) {
        super(message);
    }
}
