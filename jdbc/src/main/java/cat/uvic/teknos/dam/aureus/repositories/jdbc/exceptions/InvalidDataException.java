package cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions;

public class InvalidDataException extends RuntimeException {
    public InvalidDataException(String message) {
        super(message);
    }
}
