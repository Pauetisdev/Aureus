package cat.uvic.teknos.dam.aureus.http.exception;

/**
 * Runtime exception that carries an HTTP status code and reason phrase
 * to be returned to the client.
 */
public class HttpException extends RuntimeException {
    private final int statusCode;
    private final String reasonPhrase;

    public HttpException(int statusCode, String reasonPhrase, String message) {
        super(message);
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
    }

    /**
     * HTTP status code to return to the client (e.g. 400, 404, 500)
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Short reason phrase associated with the status code (e.g. "Bad Request").
     */
    public String getReasonPhrase() {
        return reasonPhrase;
    }
}