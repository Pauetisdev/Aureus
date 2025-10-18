package cat.uvic.teknos.dam.aureus.http;

import java.util.function.BiFunction;
import java.util.regex.Pattern;

public record Route(
        String method,
        String pathPattern,
        Pattern regex,
        BiFunction<HttpRequest, String, ResponseEntity> handler
) {
    // Constructor principal
}