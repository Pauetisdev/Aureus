package cat.uvic.teknos.dam.aureus.http;

import cat.uvic.teknos.dam.aureus.controller.CoinController;
import cat.uvic.teknos.dam.aureus.http.exception.HttpException;
import cat.uvic.teknos.dam.aureus.service.exception.EntityNotFoundException;
import com.athaydes.rawhttp.core.*;
import com.athaydes.rawhttp.core.body.StringBody;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestRouter {

    // Lista de rutas registradas para el mapeo declarativo
    private final List<Route> routes = new ArrayList<>();

    private final CoinController coinController;
    private final RawHttp rawHttp = new RawHttp();

    public RequestRouter(CoinController coinController) {
        this.coinController = coinController;
        registerRoutes();
    }

    private void registerRoutes() {
        // Rutas estáticas (/coins)
        registerRoute("GET", "/coins", null, (req, var) -> handleGetAllCoins());
        registerRoute("POST", "/coins", null, (req, var) -> handleCreateCoin(req));

        // Rutas dinámicas (/coins/{id})
        // Patrón para capturar el ID (\\d+) al final de la ruta
        Pattern idPattern = Pattern.compile("^/coins/(\\d+)$");

        registerRoute("GET", "/coins/\\d+", idPattern,
                (req, var) -> handleGetCoinById(Integer.parseInt(var)));

        registerRoute("PUT", "/coins/\\d+", idPattern,
                (req, var) -> handleUpdateCoin(Integer.parseInt(var), req));

        registerRoute("DELETE", "/coins/\\d+", idPattern,
                (req, var) -> handleDeleteCoin(Integer.parseInt(var)));
    }

    private void registerRoute(String method, String pathPattern, Pattern regex, RequestHandlerFunction handler) {
        routes.add(new Route(method, pathPattern, regex, handler));
    }

    // Este método es llamado por Server.java. Su responsabilidad es I/O y manejo de excepciones generales.
    public void handleRequest(InputStream inputStream, OutputStream outputStream) throws IOException {
        RawHttpResponse<?> response = null;

        try {
            RawHttpRequest request = rawHttp.parseRequest(inputStream);
            System.out.println("Router: " + request.getMethod() + " " + request.getUrl().getPath());

            response = route(request);

        } catch (HttpException e) {
            // 4xx errores de cliente (ej: ruta no encontrada, método no permitido)
            response = createErrorResponse(e.getStatusCode(), e.getReasonPhrase(), e.getMessage());
        } catch (EntityNotFoundException e) {
            // Excepción de negocio, mapeada a 404 HTTP
            response = createErrorResponse(404, "Not Found", e.getMessage());
        } catch (JsonSyntaxException e) {
            // Error en el cuerpo JSON (POST/PUT)
            response = createErrorResponse(400, "Bad Request", "JSON body is malformed.");
        } catch (Exception e) {
            // Cualquier otro error, incluyendo problemas de I/O o excepciones no controladas
            response = createErrorResponse(500, "Internal Server Error", "An unexpected server error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Escribir la respuesta HTTP de vuelta al stream TCP
            if (response != null) {
                response.writeTo(outputStream);
            }
        }
    }

    // Lógica de enrutamiento: busca la coincidencia declarativa
    private RawHttpResponse<?> route(RawHttpRequest request) throws Exception {
        String method = request.getMethod();
        String path = request.getUrl().getPath();

        for (Route route : routes) {
            if (route.method().equalsIgnoreCase(method)) {

                // 1. Manejo de rutas exactas (/coins)
                if (route.regex() == null && route.pathPattern().equals(path)) {
                    return route.handler().handle(request, null);
                }

                // 2. Manejo de rutas con variables (/coins/{id})
                if (route.regex() != null) {
                    Matcher matcher = route.regex().matcher(path);
                    if (matcher.matches()) {
                        // El valor de la variable es el grupo capturado por la regex (ej: el ID)
                        String pathVariable = matcher.group(1);
                        return route.handler().handle(request, pathVariable);
                    }
                }
            }
        }

        // Si el bucle termina sin encontrar coincidencia
        throw new HttpException(404, "Not Found", "Resource not found or method not allowed on this path: " + path);
    }

    // --- MANEJADORES DE ACCIONES (Delegación al Controller) ---

    private RawHttpResponse<?> handleGetAllCoins() {
        String jsonBody = coinController.getAllCoins();
        return createJsonResponseEntity(200, "OK", jsonBody);
    }

    private RawHttpResponse<?> handleGetCoinById(int id) {
        // Lanza EntityNotFoundException si la moneda no existe (capturada en handleRequest)
        String jsonBody = coinController.getCoin(id);
        return createJsonResponseEntity(200, "OK", jsonBody);
    }

    private RawHttpResponse<?> handleCreateCoin(RawHttpRequest request) {
        String body = getRequestBody(request);
        String jsonBody = coinController.createCoin(body);
        return createJsonResponseEntity(201, "Created", jsonBody);
    }

    private RawHttpResponse<?> handleUpdateCoin(int id, RawHttpRequest request) {
        String body = getRequestBody(request);
        // Nota: Asumimos que el body contiene la información de la moneda actualizada, incluyendo el ID
        coinController.updateCoin(body);
        return createEmptyResponseEntity(200, "OK");
    }

    private RawHttpResponse<?> handleDeleteCoin(int id) {
        coinController.deleteCoin(id);
        return createEmptyResponseEntity(204, "No Content");
    }

    // --- MÉTODOS AUXILIARES HTTP (Creación de Respuesta) ---

    private String getRequestBody(RawHttpRequest request) {
        // Obtiene el cuerpo de la petición.
        return request.getBody().map(b -> {
            try {
                return b.asString(StandardCharsets.UTF_8);
            } catch (IOException e) {
                // Relanzamos como RuntimeException para ser capturada en el try-catch principal
                throw new RuntimeException("Error reading request body", e);
            }
        }).orElse("");
    }

    private RawHttpResponse<?> createJsonResponseEntity(int status, String reason, String body) {
        return RawHttp.response(String.format("HTTP/1.1 %d %s", status, reason))
                .withHeaders(new Headers(
                        "Content-Type", "application/json",
                        "Content-Length", String.valueOf(body.getBytes(StandardCharsets.UTF_8).length)
                ))
                .withBody(new StringBody(body, "application/json"));
    }

    private RawHttpResponse<?> createEmptyResponseEntity(int status, String reason) {
        return RawHttp.response(String.format("HTTP/1.1 %d %s", status, reason))
                .withHeaders(new Headers("Content-Length", "0"))
                .withBody(null);
    }

    private RawHttpResponse<?> createErrorResponse(int status, String reason, String message) {
        String errorBody = String.format("{\"error\": \"%s\", \"status\": %d}", message, status);
        // Usamos createJsonResponseEntity para que el cuerpo de error también sea JSON
        return createJsonResponseEntity(status, reason, errorBody);
    }
}