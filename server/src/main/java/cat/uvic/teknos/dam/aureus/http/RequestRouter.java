package cat.uvic.teknos.dam.aureus.http;

import cat.uvic.teknos.dam.aureus.controller.CoinController;
import cat.uvic.teknos.dam.aureus.controller.CollectionController;
import cat.uvic.teknos.dam.aureus.http.exception.HttpException;
import cat.uvic.teknos.dam.aureus.service.exception.EntityNotFoundException;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple HTTP request router responsible for mapping incoming HTTP requests
 * (method + path) to handler functions that delegate to controller classes.
 *
 * <p>The router is intentionally lightweight: it parses the request using
 * {@link HttpRequest#parse} and maps routes registered at construction time
 * to small handler methods that call into {@code CoinController} and
 * {@code CollectionController}.</p>
 */
public class RequestRouter {

    // Lista de rutas registradas para el mapeo declarativo
    private final List<Route> routes = new ArrayList<>();

    private final CoinController coinController;
    private final CollectionController collectionController;

    // Constantes para el protocolo de desconexión
    public static final String DISCONNECT_PATH = "/disconnect";
    public static final String DISCONNECT_ACK_REASON = "ACK";
    public static final String DISCONNECT_ACK_BODY = "DISCONNECT_ACK";


    public RequestRouter(CoinController coinController, CollectionController collectionController) {
        this.coinController = coinController;
        this.collectionController = collectionController;
        registerRoutes();
    }

    public RequestRouter(CoinController coinController) {
        this(coinController, null);
    }

    private void registerRoutes() {
        // Rutas estáticas (/coins)
        registerRoute("GET", "/coins", null, (req, var) -> handleGetAllCoins());
        registerRoute("POST", "/coins", null, (req, var) -> handleCreateCoin(req));

        // Ruta de desconexión - la lógica especial se maneja en handleRequest
        registerRoute("GET", DISCONNECT_PATH, null, (req, var) -> {
            // Este handler es un placeholder. La lógica real (ACK + delay) está en handleRequest.
            // Si llega aquí (lo que no debería pasar en el flujo optimizado), simplemente devuelve el ACK.
            return createTextResponseEntity(200, DISCONNECT_ACK_REASON, DISCONNECT_ACK_BODY);
        });

        // Rutas para collections (si hay controller)
        if (collectionController != null) {
            registerRoute("GET", "/collections", null, (req, var) -> handleGetAllCollections());
            registerRoute("POST", "/collections", null, (req, var) -> handleCreateCollection(req));
        }

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

    private void registerRoute(String method, String pathPattern, Pattern regex, BiFunction<HttpRequest, String, ResponseEntity> handler) {
        routes.add(new Route(method, pathPattern, regex, handler));
    }

    /**
     * Handle a full HTTP request lifecycle: parse, route, and write response.
     *
     * @param inputStream source stream containing the raw HTTP request bytes
     * @param outputStream destination stream where the HTTP response will be written
     * @throws IOException if an I/O error occurs while reading or writing
     */
    // Este metodo es llamado por Server.java. Su responsabilidad es I/O y manejo de excepciones generales.
    public void handleRequest(InputStream inputStream, OutputStream outputStream) throws IOException {
        ResponseEntity response = null;

        try {
            HttpRequest request = HttpRequest.parse(inputStream);
            System.out.println("Router: " + request.getMethod() + " " + request.getPath());

            // --- MANEJO ESPECIAL DEL PROTOCOLO DE DESCONEXIÓN ---
            if (request.getMethod().equalsIgnoreCase("GET") && request.getPath().equals(DISCONNECT_PATH)) {
                System.out.println("Router: Received disconnect request. Sending acknowledgement...");

                // 1. Enviar acuse de recibo
                ResponseEntity ackResponse = createTextResponseEntity(200, DISCONNECT_ACK_REASON, DISCONNECT_ACK_BODY);
                ackResponse.writeTo(outputStream); // escribir inmediatamente
                outputStream.flush(); // asegurar la entrega

                // 2. Esperar 1 segundo
                System.out.println("Router: Waiting 1 second before allowing connection close...");
                try {
                    Thread.sleep(1000); // esperar 1 segundo (1000 ms)
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // restaurar estado interrumpido
                }

                // 3. El cierre lo gestiona el bloque finally en Server.handleClientConnection
                return; // salir de handleRequest inmediatamente
            }
            // --- FIN MANEJO ESPECIAL DE DESCONEXIÓN ---

            response = route(request); // Rutas normales para peticiones distintas de /disconnect

        } catch (HttpException e) {
            // 4xx errores del cliente (p.ej. recurso no encontrado, método no permitido)
            response = createErrorResponse(e.getStatusCode(), e.getReasonPhrase(), e.getMessage());
        } catch (EntityNotFoundException e) {
            // Excepción de negocio, mapeada a 404 HTTP
            response = createErrorResponse(404, "Not Found", e.getMessage());
        } catch (JsonSyntaxException e) {
            // Error en JSON del body
            response = createErrorResponse(400, "Bad Request", "JSON body is malformed.");
        } catch (Exception e) {
            // Cualquier otro error no previsto
            response = createErrorResponse(500, "Internal Server Error", "An unexpected server error occurred: " + e.getMessage());
            // Reemplazar printStackTrace por un logging simple
            System.err.println("Unexpected error in router: " + e.getMessage());
            e.printStackTrace(System.err);
        } finally {
            // Escribir la respuesta HTTP de vuelta al stream TCP para peticiones normales
            if (response != null) {
                response.writeTo(outputStream);
            }
        }
    }

    /**
     * Route a parsed {@link HttpRequest} to the appropriate handler.
     *
     * @param request parsed HTTP request
     * @return {@link ResponseEntity} produced by the handler
     * @throws HttpException if the path or method is not supported
     */
    // Lógica de enrutamiento: busca la coincidencia declarativa
    public ResponseEntity route(HttpRequest request) {
        String method = request.getMethod();
        String path = request.getPath();

        for (Route route : routes) {
            if (route.method().equalsIgnoreCase(method)) {

                // 1. Manejo de rutas exactas (/coins)
                if (route.regex() == null && route.pathPattern().equals(path)) {
                    return route.handler().apply(request, null);
                }

                // 2. Manejo de rutas con variables (/coins/{id})
                if (route.regex() != null) {
                    Matcher matcher = route.regex().matcher(path);
                    if (matcher.matches()) {
                        // El valor de la variable es el grupo capturado por la regex (ej: el ID)
                        String pathVariable = matcher.group(1);
                        return route.handler().apply(request, pathVariable);
                    }
                }
            }
        }

        // Si el bucle termina sin encontrar coincidencia
        throw new HttpException(404, "Not Found", "Resource not found or method not allowed on this path: " + path);
    }

    // --- MANEJADORES DE ACCIONES (Delegación al Controller) ---

    private ResponseEntity handleGetAllCoins() {
        String jsonBody = coinController.getAllCoins();
        return createJsonResponseEntity(200, "OK", jsonBody);
    }

    private ResponseEntity handleGetAllCollections() {
        String jsonBody = collectionController.getAllCollections();
        return createJsonResponseEntity(200, "OK", jsonBody);
    }

    private ResponseEntity handleGetCoinById(int id) {
        // Lanza EntityNotFoundException si la moneda no existe (capturada en handleRequest)
        String jsonBody = coinController.getCoin(id);
        return createJsonResponseEntity(200, "OK", jsonBody);
    }

    private ResponseEntity handleCreateCoin(HttpRequest request) {
        String body = getRequestBody(request);
        String jsonBody = coinController.createCoin(body);
        return createJsonResponseEntity(201, "Created", jsonBody);
    }

    private ResponseEntity handleUpdateCoin(int id, HttpRequest request) {
        String body = getRequestBody(request);
        // Usar el id de la ruta para actualizar (sobrescribe cualquier id en el body)
        coinController.updateCoin(id, body);
        return createEmptyResponseEntity(200, "OK");
    }

    private ResponseEntity handleDeleteCoin(int id) {
        coinController.deleteCoin(id);
        return createEmptyResponseEntity(204, "No Content");
    }

    private ResponseEntity handleCreateCollection(HttpRequest request) {
        String body = getRequestBody(request);
        try {
            String jsonBody = collectionController.createCollection(body);
            return createJsonResponseEntity(201, "Created", jsonBody);
        } catch (IllegalArgumentException e) {
            return createErrorResponse(400, "Bad Request", e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error creating collection: " + e.getMessage());
            e.printStackTrace(System.err);
            return createErrorResponse(500, "Internal Server Error", "An unexpected error occurred while creating collection");
        }
    }

    // --- MÉTODOS AUXILIARES HTTP (Creación de Respuesta) ---

    private String getRequestBody(HttpRequest request) {
        return request.getBody();
    }

    private ResponseEntity createJsonResponseEntity(int status, String reason, String body) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        byte[] bodyBytes = body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8);
        headers.put("Content-Length", String.valueOf(bodyBytes.length));
        return new ResponseEntity(status, reason, headers, bodyBytes);
    }

    private ResponseEntity createTextResponseEntity(int status, String reason, String body) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "text/plain");
        byte[] bodyBytes = body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8);
        headers.put("Content-Length", String.valueOf(bodyBytes.length));
        return new ResponseEntity(status, reason, headers, bodyBytes);
    }


    private ResponseEntity createEmptyResponseEntity(int status, String reason) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Length", "0");
        return new ResponseEntity(status, reason, headers, null);
    }

    private ResponseEntity createErrorResponse(int status, String reason, String message) {
        String errorBody = String.format("{\"error\": \"%s\", \"status\": %d}", message, status);
        return createJsonResponseEntity(status, reason, errorBody);
    }
}