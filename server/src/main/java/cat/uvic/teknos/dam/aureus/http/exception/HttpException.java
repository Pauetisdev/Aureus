package cat.uvic.teknos.dam.aureus.http.exception;

public class HttpException extends RuntimeException {
    private final int statusCode;
    private final String reasonPhrase;

    public HttpException(int statusCode, String reasonPhrase, String message) {
        super(message);
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }
}