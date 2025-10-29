package cat.uvic.teknos.dam.aureus.http;

import java.util.function.BiFunction;
import java.util.regex.Pattern;

/**
 * Simple route description used by {@link RequestRouter} to match method/path
 * and to invoke a handler function.
 *
 * @param method HTTP method (GET, POST, PUT, DELETE)
 * @param pathPattern declared path pattern (string equality or regex-like pattern)
 * @param regex optional compiled regex used to extract path variables
 * @param handler handler function receiving the parsed {@link HttpRequest}
 *                and an optional path variable
 */
public record Route(
        String method,
        String pathPattern,
        Pattern regex,
        BiFunction<HttpRequest, String, ResponseEntity> handler
) {
    // Constructor principal
}