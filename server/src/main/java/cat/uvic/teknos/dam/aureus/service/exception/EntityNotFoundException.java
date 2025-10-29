package cat.uvic.teknos.dam.aureus.service.exception;

/**
 * Exception thrown when a requested entity cannot be found in the data store.
 */
public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}