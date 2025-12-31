package cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}