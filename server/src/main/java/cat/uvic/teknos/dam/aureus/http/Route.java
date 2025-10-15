package cat.uvic.teknos.dam.aureus.http;

import java.util.regex.Pattern;

public record Route(
        String method,
        String pathPattern,
        Pattern regex,
        RequestHandlerFunction handler
) {
    // Constructor principal
}