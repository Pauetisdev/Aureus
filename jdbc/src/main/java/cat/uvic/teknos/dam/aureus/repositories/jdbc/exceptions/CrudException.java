package cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions;

public class CrudException extends RuntimeException {
    public CrudException(String message, Throwable cause) {
        super(message, cause);
    }

    public CrudException(Throwable cause) {
        super(cause);
    }
}
