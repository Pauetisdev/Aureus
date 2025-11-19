package cat.uvic.teknos.dam.aureus;

import cat.uvic.teknos.dam.aureus.security.CryptoUtils;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Client application for interacting with the Aureus HTTP server.
 *
 * <p>This client opens a socket per request (simple HTTP/1.1 style),
 * monitors user activity locally, and performs a graceful disconnect
 * when the client has been inactive for a configured timeout.</p>
 */
public class Client {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());
    private final String host;
    private final int port;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<Integer, String> collectionNameCache = new HashMap<>();

    // Marca de tiempo de la última actividad (lectura/entrada del usuario/operación de E/S)
    private volatile long lastActivityTime = System.currentTimeMillis();
    // Scheduler para monitorizar inactividad (hilo daemon para no bloquear el cierre de la JVM)
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "client-inactivity-monitor");
        t.setDaemon(true);
        return t;
    });
    // Timeout de inactividad configurable (2 minutos por requerimiento)
    // Temporalmente 10 segundos para pruebas locales; revertir a 2 minutos antes de entrega
    private final long INACTIVITY_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(2); // 2 minutes inactivity timeout as required

    private final AtomicBoolean running = new AtomicBoolean(true);



    private static final String DISCONNECT_PATH = "/disconnect";
    private static final String DISCONNECT_ACK_BODY = "DISCONNECT_ACK";

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Run the interactive console client.
     *
     * <p>Starts an inactivity monitor and then enters the user input loop.
     * The method returns when the client exits (via menu or inactivity).</p>
     */
    public void run() {
        Scanner sc = new Scanner(System.in);

        System.out.println("+-----------------------------------------------+");
        System.out.println("|     AUREUS - Coin Management System Client    |");
        System.out.println("|     Connected to: " + host + ":" + port + "               |");
        System.out.println("+-----------------------------------------------+");
        System.out.println("| INACTIVITY MONITOR: Active (2 minutes timeout)  |");
        System.out.println("+-----------------------------------------------+");

        // Programar el monitor de inactividad: se ejecuta cada 5 segundos
        scheduler.scheduleAtFixedRate(this::checkInactivity, 5, 5, TimeUnit.SECONDS);

        while (running.get()) {
            printMenu();
            System.out.print("Select an option: ");
            lastActivityTime = System.currentTimeMillis();

            if (!sc.hasNextLine() && !running.get()) {
                break;
            }
            String option = sc.nextLine().trim();
            lastActivityTime = System.currentTimeMillis();

            try {
                switch (option) {
                    case "1":
                        listAllCoins();
                        break;
                    case "2":
                        getCoinById(sc);
                        break;
                    case "3":
                        createCoin(sc);
                        break;
                    case "4":
                        updateCoin(sc);
                        break;
                    case "5":
                        deleteCoin(sc);
                        break;
                    case "0":
                        handleGracefulExit();
                        return;
                    default:
                        System.out.println("\nInvalid option. Please try again.\n");
                }
            } catch (Exception e) {
                System.out.println("\nError: " + e.getMessage() + "\n");
            }

            if (running.get()) {
                waitForEnter(sc);
            }
        }

        shutdown();
    }

    // Comprueba si el cliente ha estado inactivo y, si es así, inicia la desconexión
    private void checkInactivity() {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastActivityTime;

        if (elapsed >= INACTIVITY_TIMEOUT_MS) {
            if (running.compareAndSet(true, false)) {
                System.out.println("\n\n*** INACTIVITY DETECTED ***");
                System.out.println("Client has been inactive for more than 2 minutes. Initiating graceful disconnection...");
                try {
                    Response response = sendRequest("GET", DISCONNECT_PATH, null);

                    if (response != null && response.body != null && response.body.trim().equals(DISCONNECT_ACK_BODY)) {
                        System.out.println("Server acknowledged disconnection: " + response.statusLine);
                    } else {
                        System.err.println("Server did not send expected acknowledgement for disconnect. Status line: " + (response != null ? response.statusLine : "No response"));
                    }
                } catch (IOException e) {
                    System.err.println("Error during graceful disconnection: " + e.getMessage());
                }
                // Mensaje de salida y limpieza fuera de finally para evitar advertencias
                System.out.println("Exiting client application.");
                shutdown();
                System.exit(0);
            }
        }
    }

    // Maneja la salida explícita desde el menú (envía DISCONNECT antes de cerrar)
    private void handleGracefulExit() {
        if (running.compareAndSet(true, false)) {
            System.out.println("\nGoodbye! Closing connection...");
            try {
                System.out.println("Sending disconnect signal to server...");
                Response response = sendRequest("GET", DISCONNECT_PATH, null);
                // Esperar y comprobar el ACK del servidor antes de cerrar
                if (response != null && response.body != null && response.body.trim().equals(DISCONNECT_ACK_BODY)) {
                    System.out.println("Server acknowledged disconnection: " + response.statusLine);
                } else {
                    System.err.println("Server did not send expected acknowledgement for disconnect. Status line: " + (response != null ? response.statusLine : "No response"));
                }
            } catch (IOException e) {
                // Ignorar errores al notificar; procedemos a apagar el cliente
                System.err.println("Warning: error while notifying server of disconnect: " + e.getMessage());
            } finally {
                shutdown();
            }
        }
    }

    // Apaga el scheduler del monitor de inactividad
    private void shutdown() {
        scheduler.shutdownNow();
    }

    /**
     * Send a simple HTTP request to the server and return the parsed response.
     *
     * <p>The implementation opens a new TCP connection for each request,
     * writes the HTTP request, reads headers to get Content-Length, then
     * reads the response body. The method updates the client's lastActivityTime.</p>
     *
     * @param method HTTP method (e.g., GET, POST)
     * @param path   Request path starting with '/'
     * @param body   Optional request body (JSON string) or null
     * @return Response object containing status line and body text
     * @throws IOException on network or I/O errors
     */
    private Response sendRequest(String method, String path, String body) throws IOException {
        try (Socket socket = new Socket(host, port)) {
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            ByteArrayOutputStream reqOut = new ByteArrayOutputStream();
            StringBuilder sb = new StringBuilder();
            sb.append(method).append(" ").append(path).append(" HTTP/1.1\r\n");
            sb.append("Host: ").append(host).append(":").append(port).append("\r\n");
            if (body != null && !body.isEmpty()) {
                byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
                sb.append("Content-Type: application/json\r\n");
                sb.append("Content-Length: ").append(bodyBytes.length).append("\r\n");
                // Add X-Body-Hash header for message integrity
                String bodyHash = CryptoUtils.hash(bodyBytes);
                // Debug logging: outgoing body hash
                LOGGER.log(Level.FINE, "Client: sending body hash = {0}", bodyHash);
                sb.append("X-Body-Hash").append(": ").append(bodyHash).append("\r\n");
                sb.append("\r\n");
                reqOut.write(sb.toString().getBytes(StandardCharsets.UTF_8));
                reqOut.write(bodyBytes);
            } else {
                sb.append("Content-Length: 0\r\n");
                sb.append("\r\n");
                reqOut.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            }

            out.write(reqOut.toByteArray());
            out.flush();

            lastActivityTime = System.currentTimeMillis();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String statusLine = reader.readLine();
            if (statusLine == null) throw new IOException("No response from server");
            String line;
            int contentLength = 0;
            String respBodyHash = null;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                String[] headerParts = line.split(":", 2);
                if (headerParts.length == 2) {
                    String name = headerParts[0].trim();
                    String value = headerParts[1].trim();
                    if (name.equalsIgnoreCase("Content-Length")) {
                        try { contentLength = Integer.parseInt(value); } catch (NumberFormatException ignored) {}
                    }
                    if (name.equalsIgnoreCase("X-Body-Hash")) {
                        respBodyHash = value;
                    }
                }
            }
            char[] buf = new char[contentLength];
            int read = 0;
            while (read < contentLength) {
                int r = reader.read(buf, read, contentLength - read);
                if (r == -1) break;
                read += r;
            }
            String bodyResp = new String(buf, 0, read);

            // Verify response body hash if present
            if (contentLength > 0) {
                String computedRespHash = CryptoUtils.hash(bodyResp);
                // Debug logging: response hashes
                LOGGER.log(Level.FINE, "Client: received response body hash header = {0}", (respBodyHash == null ? "<missing>" : respBodyHash));
                LOGGER.log(Level.FINE, "Client: computed response body hash = {0}", computedRespHash);
                if (respBodyHash == null || !respBodyHash.equalsIgnoreCase(computedRespHash)) {
                    throw new IOException("Invalid response body hash");
                }
            }

            lastActivityTime = System.currentTimeMillis();

            return new Response(statusLine, bodyResp);
        }
    }

    // Espera a que el usuario pulse Enter (actualiza la marca de actividad)
    private void waitForEnter(Scanner sc) {
        System.out.print("\nPress Enter to continue...");
        lastActivityTime = System.currentTimeMillis();
        sc.nextLine();
        lastActivityTime = System.currentTimeMillis();
    }

    private void printFormattedResponse(Response response) {
        String body = response.body == null ? "" : response.body.trim();
        // If HTTP status indicates error, print a concise error message instead of building tables
        int status = parseStatusCode(response.statusLine);
        if (status >= 400) {
            String msg = null;
            try {
                if (body.startsWith("{")) {
                    Map<String,Object> err = mapper.readValue(body, new TypeReference<>(){});
                    Object maybe = err.get("Error");
                    if (maybe == null) maybe = err.get("error");
                    if (maybe == null) maybe = err.get("message");
                    if (maybe != null) msg = String.valueOf(maybe);
                }
            } catch (Exception ignored) {}
            if (msg == null || msg.isEmpty()) msg = body.isEmpty() ? ("HTTP " + status + " " + response.statusLine) : body;
            System.out.println("Error: " + msg + " (HTTP " + status + ")");
            return;
        }
        // Success: print status line then body/table
        System.out.println(response.statusLine);
        if (body.isEmpty()) {
            System.out.println("(no body)");
            return;
        }

        try {
            if (body.startsWith("[")) {
                List<Map<String, Object>> list = mapper.readValue(body, new TypeReference<>(){});
                if (list.isEmpty()) {
                    System.out.println("[]");
                    return;
                }
                LinkedHashSet<String> keys = new LinkedHashSet<>();
                for (Map<String, Object> m : list) keys.addAll(m.keySet());

                List<String> headers = new ArrayList<>(keys);
                // Crear versión visual de los encabezados (p. ej. coinName -> "Coin Name")
                List<String> displayHeaders = new ArrayList<>();
                for (String h : headers) displayHeaders.add(formatHeaderKey(h));
                List<List<String>> rowsData = new ArrayList<>();
                for (Map<String, Object> m : list) {
                    List<String> row = new ArrayList<>();
                    for (String h : headers) {
                        Object raw = m.get(h);
                        if ("collection".equalsIgnoreCase(h)) {
                            row.add(formatCollectionValue(raw));
                        } else {
                            row.add(formatValue(raw));
                        }
                    }
                    rowsData.add(row);
                }
                printAsciiTable(displayHeaders, rowsData);
                return;
            } else if (body.startsWith("{")) {
                Map<String, Object> map = mapper.readValue(body, new TypeReference<>(){});
                LinkedHashSet<String> keys = new LinkedHashSet<>(map.keySet());

                List<String> headers = new ArrayList<>(keys);
                List<String> displayHeaders = new ArrayList<>();
                for (String h : headers) displayHeaders.add(formatHeaderKey(h));
                List<List<String>> rowsData = new ArrayList<>();
                List<String> single = new ArrayList<>();
                for (String h : headers) {
                    Object raw = map.get(h);
                    if ("collection".equalsIgnoreCase(h)) single.add(formatCollectionValue(raw));
                    else single.add(formatValue(raw));
                }
                rowsData.add(single);
                printAsciiTable(displayHeaders, rowsData);
                return;
            }
        } catch (Exception e) {
            // Evitar catch vacío: registrar un mensaje de ayuda en caso de error durante el formateo
            System.err.println("Response formatting error: " + e.getMessage());
        }

        System.out.println(body);
    }

    // Convierte las claves JSON en etiquetas legibles para el usuario
    private String formatHeaderKey(String key) {
        if (key == null) return "";
        switch (key) {
            case "id":
            case "coinId":
                return "ID";
            case "coinName":
                return "Coin Name";
            case "coinYear":
                return "Coin Year";
            case "coinMaterial":
                return "Coin Material";
            case "coinWeight":
                return "Coin Weight";
            case "coinDiameter":
                return "Coin Diameter";
            case "estimatedValue":
                return "Estimated Value";
            case "originCountry":
                return "Origin Country";
            case "historicalSignificance":
            case "description":
                return "Description";
            case "collectionId":
                return "Collection ID";
            case "collectionName":
                return "Collection Name";
            case "collection":
                return "Collection";
            default:
                // Capitalizar palabras separadas por camelCase o snake_case
                String s = key.replaceAll("_", " ");
                // Insert space before capital letters (camelCase to words)
                s = s.replaceAll("([a-z])([A-Z])", "$1 $2");
                String[] parts = s.split("\\s+");
                StringBuilder out = new StringBuilder();
                for (int i = 0; i < parts.length; i++) {
                    String p = parts[i];
                    if (p.isEmpty()) continue;
                    out.append(Character.toUpperCase(p.charAt(0)));
                    if (p.length() > 1) out.append(p.substring(1));
                    if (i < parts.length - 1) out.append(' ');
                }
                return out.toString();
        }
    }

    private String formatValue(Object val) {
        if (val == null) return "";
        try {
            if (val instanceof Map) {
                Map<?,?> mm = (Map<?,?>) val;
                Object maybeNested = mm.get("collection");
                if (maybeNested instanceof Map) mm = (Map<?,?>) maybeNested;

                Object id = mm.get("id");
                if (id == null) id = mm.get("collectionId");
                if (id == null) id = mm.get("collection_id");
                if (id == null) id = mm.get("coinId");

                Object name = mm.get("collectionName");
                if (name == null) name = mm.get("name");
                if (name == null) name = mm.get("collection_name");

                if (id == null && name == null) {
                    for (Map.Entry<?,?> e : mm.entrySet()) {
                        String k = String.valueOf(e.getKey()).toLowerCase();
                        if (k.contains("name") || k.contains("title") || k.contains("collection")) {
                            name = e.getValue();
                            break;
                        }
                    }
                }

                if (id == null && name == null) return String.valueOf(mm);
                if (name == null) return String.valueOf(id == null ? "" : id);
                if (id == null) return String.valueOf(name);
                return String.valueOf(id) + ": " + String.valueOf(name);
            }
            if (val instanceof List) {
                List<?> lst = (List<?>) val;
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < lst.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append(lst.get(i) == null ? "" : String.valueOf(lst.get(i)));
                }
                return sb.toString();
            }
            return String.valueOf(val);
        } catch (Throwable t) {
            return String.valueOf(val);
        }
    }

    private String formatCollectionValue(Object raw) {
        if (raw == null) return "";
        try {
            if (raw instanceof Map) {
                Map<?,?> mm = (Map<?,?>) raw;
                Object idObj = mm.get("id");
                if (idObj == null) idObj = mm.get("collectionId");
                Object nameObj = mm.get("collectionName");
                if (nameObj == null) nameObj = mm.get("name");
                if (idObj instanceof Number) {
                    int id = ((Number) idObj).intValue();
                    if (nameObj != null) return id + ": " + String.valueOf(nameObj);
                    return fetchCollectionDisplay(id);
                }
                return formatValue(raw);
            }

            if (raw instanceof Number) {
                int id = ((Number) raw).intValue();
                return fetchCollectionDisplay(id);
            }
            String s = String.valueOf(raw).trim();
            if (s.matches("\\d+")) {
                int id = Integer.parseInt(s);
                return fetchCollectionDisplay(id);
            }
            return formatValue(raw);
        } catch (Throwable t) {
            return String.valueOf(raw);
        }
    }

    private String fetchCollectionDisplay(int id) {
        if (collectionNameCache.containsKey(id)) {
            String name = collectionNameCache.get(id);
            return name == null || name.isEmpty() ? String.valueOf(id) : (id + ": " + name);
        }
        try {
            Response r = sendRequest("GET", "/collections/" + id, null);
            String body = r.body == null ? "" : r.body.trim();
            if (body.isEmpty()) {
                collectionNameCache.put(id, "");
                return String.valueOf(id);
            }
            Map<String,Object> m = mapper.readValue(body, new TypeReference<>(){});
            Object name = m.get("collectionName");
            if (name == null) name = m.get("name");
            String sname = name == null ? "" : String.valueOf(name);
            collectionNameCache.put(id, sname);
            return sname.isEmpty() ? String.valueOf(id) : (id + ": " + sname);
        } catch (Exception e) {
            collectionNameCache.put(id, "");
            return String.valueOf(id);
        }
    }

    private static class Response {
        public final String statusLine;
        public final String body;

        public Response(String statusLine, String body) {
            this.statusLine = statusLine;
            this.body = body;
        }
    }

    private void printAsciiTable(List<String> headers, List<List<String>> rows) {
        // Add spacing before table for readability
        System.out.println();
        final int MAX_COL_WIDTH = 30;

        int cols = headers.size();
        int[] widths = new int[cols];

        for (int i = 0; i < cols; i++) widths[i] = Math.min(headers.get(i) == null ? 0 : headers.get(i).length(), MAX_COL_WIDTH);

        for (List<String> r : rows) {
            for (int i = 0; i < cols; i++) {
                String cell = i < r.size() && r.get(i) != null ? r.get(i) : "";
                widths[i] = Math.max(widths[i], Math.min(cell.length(), MAX_COL_WIDTH));
            }
        }

        StringBuilder sep = new StringBuilder("+");
        for (int w : widths) {
            sep.append(String.join("", Collections.nCopies(w + 2, "-"))).append("+");
        }

        System.out.println(sep.toString());
        StringBuilder headerLine = new StringBuilder("|");
        for (int i = 0; i < cols; i++) {
            headerLine.append(' ').append(formatCell(headers.get(i), widths[i], false)).append(' ').append('|');
        }
        System.out.println(headerLine.toString());

        StringBuilder headerSep = new StringBuilder("+");
        for (int w : widths) {
            headerSep.append(String.join("", Collections.nCopies(w + 2, "="))).append("+");
        }
        System.out.println(headerSep.toString());

        for (List<String> r : rows) {
            StringBuilder line = new StringBuilder("|");
            for (int i = 0; i < cols; i++) {
                String cell = i < r.size() && r.get(i) != null ? r.get(i) : "";
                boolean right = isNumeric(cell);
                line.append(' ').append(formatCell(cell, widths[i], right)).append(' ').append('|');
            }
            System.out.println(line.toString());
        }

        System.out.println(sep.toString());
        // Add spacing after table
        System.out.println();
    }

    private String formatCell(String s, int width, boolean rightAlign) {
        if (s == null) s = "";
        if (s.length() > width) {
            if (width <= 3) s = s.substring(0, width);
            else s = s.substring(0, width - 3) + "...";
        }
        if (s.length() == width) return s;
        int pad = width - s.length();
        if (rightAlign) {
            return String.join("", Collections.nCopies(pad, " ")) + s;
        } else {
            return s + String.join("", Collections.nCopies(pad, " "));
        }
    }

    private boolean isNumeric(String s) {
        if (s == null || s.isEmpty()) return false;
        try {
            Double.parseDouble(s.replaceAll(",", ""));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void showAvailableIds() {
        try {
            Response response = sendRequest("GET", "/coins", null);
            String body = response.body == null ? "" : response.body.trim();
            if (body.isEmpty()) {
                System.out.println("(no coins)");
                return;
            }
            if (!body.startsWith("[")) {
                Map<String, Object> single = mapper.readValue(body, new TypeReference<>(){});
                Object id = single.get("id");
                Object name = single.get("coinName");
                System.out.println("Available coins:");
                System.out.println("ID | Name");
                System.out.println((id == null ? "" : id.toString()) + " | " + (name == null ? "" : name.toString()));
                return;
            }

            List<Map<String, Object>> list = mapper.readValue(body, new TypeReference<>(){});
            if (list.isEmpty()) {
                System.out.println("(no coins)");
                return;
            }
            List<String> headers = Arrays.asList("ID", "Name");
            List<List<String>> rows = new ArrayList<>();
            for (Map<String, Object> m : list) {
                Object id = m.get("id");
                Object name = m.get("coinName");
                rows.add(Arrays.asList(id == null ? "" : String.valueOf(id), name == null ? "" : String.valueOf(name)));
            }
            printAsciiTable(headers, rows);
        } catch (Exception e) {
            System.out.println("Could not fetch coin list: " + e.getMessage());
        }
    }

    // ----------------- MÉTODOS AGREGADOS -----------------

    private void printMenu() {
        System.out.println("\n--- MENU ---");
        System.out.println("1) List all coins");
        System.out.println("2) Get coin by ID");
        System.out.println("3) Create coin");
        System.out.println("4) Update coin");
        System.out.println("5) Delete coin");
        System.out.println("0) Exit");
    }

    private void listAllCoins() {
        try {
            Response r = sendRequest("GET", "/coins", null);
            printFormattedResponse(r);
        } catch (Exception e) {
            System.out.println("Error listing coins: " + e.getMessage());
        }
    }

    private void getCoinById(Scanner sc) {
        try {
            // Show available IDs before asking for the ID
            System.out.println();
            showAvailableIds();
            System.out.print("Enter coin ID: ");
            String id = sc.nextLine().trim();
            if (id.isEmpty()) {
                System.out.println("ID required.");
                return;
            }
            Response r = sendRequest("GET", "/coins/" + id, null);
            int code = parseStatusCode(r.statusLine);
            if (code >= 400) {
                // Try to extract a helpful error message from the body if it's JSON
                String body = r.body == null ? "" : r.body.trim();
                String msg = null;
                try {
                    if (body.startsWith("{")) {
                        Map<String,Object> err = mapper.readValue(body, new TypeReference<>(){});
                        Object maybe = err.get("Error");
                        if (maybe == null) maybe = err.get("error");
                        if (maybe == null) maybe = err.get("message");
                        if (maybe != null) msg = String.valueOf(maybe);
                    }
                } catch (Exception ignored) {}
                if (msg == null || msg.isEmpty()) msg = (r.body == null || r.body.trim().isEmpty()) ? r.statusLine : r.body.trim();
                System.out.println("Error: " + msg + " (HTTP " + code + ")");
                return;
            }
            printFormattedResponse(r);
        } catch (Exception e) {
            System.out.println("Error fetching coin: " + e.getMessage());
        }
    }

    private void showAvailableCollections() {
        try {
            Response response = sendRequest("GET", "/collections", null);
            String body = response.body == null ? "" : response.body.trim();
            if (body.isEmpty()) {
                System.out.println("(no collections)");
                return;
            }
            if (!body.startsWith("[")) {
                Map<String, Object> single = mapper.readValue(body, new TypeReference<>(){});
                Object id = single.get("id");
                Object name = single.get("collectionName");
                if (name == null) name = single.get("name");
                System.out.println("Available collections:");
                System.out.println("ID | Name");
                System.out.println((id == null ? "" : id.toString()) + " | " + (name == null ? "" : name.toString()));
                return;
            }

            List<Map<String, Object>> list = mapper.readValue(body, new TypeReference<>(){});
            if (list.isEmpty()) {
                System.out.println("(no collections)");
                return;
            }
            List<String> headers = Arrays.asList("ID", "Name");
            List<List<String>> rows = new ArrayList<>();
            for (Map<String, Object> m : list) {
                Object id = m.get("id");
                Object name = m.get("collectionName");
                if (name == null) name = m.get("name");
                rows.add(Arrays.asList(id == null ? "" : String.valueOf(id), name == null ? "" : String.valueOf(name)));
            }
            printAsciiTable(headers, rows);
        } catch (Exception e) {
            System.out.println("Could not fetch collection list: " + e.getMessage());
        }
    }

    private void createCoin(Scanner sc) {
        try {
            System.out.println();
            System.out.print("Coin Name: ");
            String name = toTitleCase(sc.nextLine().trim());

            System.out.print("Coin Year (numeric): ");
            String yearStr = sc.nextLine().trim();

            System.out.print("Coin Material: ");
            String material = sc.nextLine().trim();

            System.out.print("Coin Weight (numeric, e.g. 12.5): ");
            String weightStr = sc.nextLine().trim();

            System.out.print("Coin Diameter (numeric, e.g. 20.0): ");
            String diameterStr = sc.nextLine().trim();

            System.out.print("Estimated Value (numeric, e.g. 100.0): ");
            String valueStr = sc.nextLine().trim();

            System.out.print("Origin Country: ");
            String origin = sc.nextLine().trim();

            System.out.print("Description (optional): ");
            String historical = sc.nextLine().trim();

            // Show available collections before asking for collectionId
            System.out.println();
            showAvailableCollections();

            System.out.print("Collection ID: ");
            String collStr = sc.nextLine().trim();

            Map<String,Object> payload = new LinkedHashMap<>();
            if (!name.isEmpty()) payload.put("coinName", name);
            if (!yearStr.isEmpty()) {
                try { payload.put("coinYear", Integer.parseInt(yearStr)); } catch (NumberFormatException ignored) {}
            }
            if (!material.isEmpty()) payload.put("coinMaterial", material);
            if (!weightStr.isEmpty()) {
                try { payload.put("coinWeight", Double.parseDouble(weightStr)); } catch (NumberFormatException ignored) {}
            }
            if (!diameterStr.isEmpty()) {
                try { payload.put("coinDiameter", Double.parseDouble(diameterStr)); } catch (NumberFormatException ignored) {}
            }
            if (!valueStr.isEmpty()) {
                try { payload.put("estimatedValue", Double.parseDouble(valueStr)); } catch (NumberFormatException ignored) {}
            }
            if (!origin.isEmpty()) payload.put("originCountry", origin);
            if (!historical.isEmpty()) {
                payload.put("historicalSignificance", historical);
                payload.put("description", historical);
            }
            if (!collStr.isEmpty()) {
                try { payload.put("collectionId", Integer.parseInt(collStr)); } catch (NumberFormatException ignored) {}
            }

            String body = mapper.writeValueAsString(payload);
            Response r = sendRequest("POST", "/coins", body);
            // Print server response (created coin or message)
            printFormattedResponse(r);

            // If POST was successful but server did not return a body with new coin details,
            // try to fetch created coin if the response contains an "id" field or Location header is not available.
            int code = parseStatusCode(r.statusLine);
            if (code >= 200 && code < 300) {
                // If response body empty or not JSON object, we won't automatically fetch (to avoid guessing id).
                // The server usually returns created entity; if not, user can list coins to see it.
            }
        } catch (Exception e) {
            System.out.println("Error creating coin: " + e.getMessage());
        }
    }

    private void updateCoin(Scanner sc) {
        try {
            // Show available IDs before asking for the ID
            System.out.println();
            showAvailableIds();
            System.out.print("Enter coin ID to update: ");
            String id = sc.nextLine().trim();
            if (id.isEmpty()) {
                System.out.println("ID required.");
                return;
            }

            // Get existing entity
            Response getResp = sendRequest("GET", "/coins/" + id, null);
            int getCode = parseStatusCode(getResp.statusLine);
            if (getCode >= 400) {
                String body = getResp.body == null ? "" : getResp.body.trim();
                String msg = null;
                try {
                    if (body.startsWith("{")) {
                        Map<String,Object> err = mapper.readValue(body, new TypeReference<>(){});
                        Object maybe = err.get("Error");
                        if (maybe == null) maybe = err.get("error");
                        if (maybe == null) maybe = err.get("message");
                        if (maybe != null) msg = String.valueOf(maybe);
                    }
                } catch (Exception ignored) {}
                if (msg == null || msg.isEmpty()) msg = body.isEmpty() ? getResp.statusLine : body;
                System.out.println("Error fetching existing coin: " + msg + " (HTTP " + getCode + ")");
                return;
            }

            String getBody = getResp.body == null ? "" : getResp.body.trim();
            Map<String,Object> existing = mapper.readValue(getBody, new TypeReference<>(){});
            System.out.println("Current coin data:");
            printFormattedResponse(getResp);

            // Ask user for new values
            System.out.print("Coin Name (leave blank to keep): ");
            String name = toTitleCase(sc.nextLine().trim());

            System.out.print("Coin Year (numeric, leave blank to keep): ");
            String yearStr = sc.nextLine().trim();

            System.out.print("Coin Material (leave blank to keep): ");
            String material = sc.nextLine().trim();

            System.out.print("Coin Weight (numeric, leave blank to keep, e.g. 12.5): ");
            String weightStr = sc.nextLine().trim();

            System.out.print("Coin Diameter (numeric, leave blank to keep, e.g. 20.0): ");
            String diameterStr = sc.nextLine().trim();

            System.out.print("Estimated Value (numeric, leave blank to keep, e.g. 100.0): ");
            String valueStr = sc.nextLine().trim();

            System.out.print("Origin Country (leave blank to keep): ");
            String origin = sc.nextLine().trim();

            System.out.print("Description (leave blank to keep): ");
            String historical = sc.nextLine().trim();

            System.out.println();
            showAvailableCollections();

            System.out.print("Collection ID (numeric, leave blank to keep): ");
            String collStr = sc.nextLine().trim();

            // Build payload from allowed primitive fields only (avoid sending nested objects)
            Map<String,Object> payload = extractExistingFieldsForUpdate(existing);

            if (!name.isEmpty()) payload.put("coinName", name);
            if (!yearStr.isEmpty()) {
                try { payload.put("coinYear", Integer.parseInt(yearStr)); } catch (NumberFormatException ignored) {}
            }
            if (!material.isEmpty()) payload.put("coinMaterial", material);
            if (!weightStr.isEmpty()) {
                try { payload.put("coinWeight", Double.parseDouble(weightStr)); } catch (NumberFormatException ignored) {}
            }
            if (!diameterStr.isEmpty()) {
                try { payload.put("coinDiameter", Double.parseDouble(diameterStr)); } catch (NumberFormatException ignored) {}
            }
            if (!valueStr.isEmpty()) {
                try { payload.put("estimatedValue", Double.parseDouble(valueStr)); } catch (NumberFormatException ignored) {}
            }
            if (!origin.isEmpty()) payload.put("originCountry", origin);
            if (!historical.isEmpty()) {
                payload.put("historicalSignificance", historical);
                payload.put("description", historical);
            }
            if (!collStr.isEmpty()) {
                try { payload.put("collectionId", Integer.parseInt(collStr)); } catch (NumberFormatException ignored) {}
            }

            String body = mapper.writeValueAsString(payload);
            Response r = sendRequest("PUT", "/coins/" + id, body);
            int code = parseStatusCode(r.statusLine);

            if (code >= 200 && code < 300) {
                // After successful update, fetch and display the updated coin to guarantee user sees latest data
                try {
                    Response refreshed = sendRequest("GET", "/coins/" + id, null);
                    printFormattedResponse(refreshed);
                } catch (Exception e) {
                    // If refresh fails, show the original PUT response
                    printFormattedResponse(r);
                }
            } else {
                printFormattedResponse(r);
            }
        } catch (Exception e) {
            System.out.println("Error updating coin: " + e.getMessage());
        }
    }

    private void deleteCoin(Scanner sc) {
        try {
            // Show available IDs before asking for the ID
            System.out.println();
            showAvailableIds();
            System.out.print("Enter coin ID to delete: ");
            String id = sc.nextLine().trim();
            if (id.isEmpty()) {
                System.out.println("ID required.");
                return;
            }

            // Validate existencia del coin sin volver a mostrar la lista en caso de error
            Response existingResp;
            try {
                Response check = sendRequest("GET", "/coins/" + id, null);
                int code = parseStatusCode(check.statusLine);
                if (code != 200) {
                    String body = check.body == null ? "" : check.body.trim();
                    String msg = null;
                    try {
                        if (body.startsWith("{")) {
                            Map<String,Object> err = mapper.readValue(body, new TypeReference<>(){});
                            Object maybe = err.get("Error");
                            if (maybe == null) maybe = err.get("error");
                            if (maybe == null) maybe = err.get("message");
                            if (maybe != null) msg = String.valueOf(maybe);
                        }
                    } catch (Exception ignored) {}
                    if (msg == null || msg.isEmpty()) msg = "Coin ID " + id + " not found.";
                    System.out.println("Error: " + msg + " (HTTP " + code + ")");
                    return;
                }
                existingResp = check; // guardar los datos del coin existente para mostrar después de eliminar con éxito
            } catch (IOException ioe) {
                System.out.println("Error checking coin existence: " + ioe.getMessage());
                return;
            }

            // Confirmation (English)
            System.out.print("Are you sure you want to delete the coin with ID " + id + "? (y/N): ");
            String confirm = sc.nextLine().trim();
            if (!confirm.equalsIgnoreCase("y") && !confirm.equalsIgnoreCase("yes")) {
                System.out.println("Deletion cancelled.");
                return;
            }

            Response r = sendRequest("DELETE", "/coins/" + id, null);
            int code = parseStatusCode(r.statusLine);
            if (code >= 200 && code < 300) {
                // Éxito: mostrar la tabla con la información que se eliminó (de existingResp) y imprimir la confirmación
                printFormattedResponse(existingResp);
                System.out.println("Coin with ID " + id + " deleted successfully.");
            } else {
                printFormattedResponse(r);
            }
        } catch (Exception e) {
            System.out.println("Error deleting coin: " + e.getMessage());
        }
    }

    // Helper: build payload for update from existing map but only include allowed primitive fields (no nested objects)
    private Map<String,Object> extractExistingFieldsForUpdate(Map<String,Object> existing) {
        Map<String,Object> out = new LinkedHashMap<>();
        if (existing == null) return out;
        putIfPresent(out, "coinName", existing.get("coinName"));
        putIfPresent(out, "coinYear", existing.get("coinYear"));
        putIfPresent(out, "coinMaterial", existing.get("coinMaterial"));
        putIfPresent(out, "coinWeight", existing.get("coinWeight"));
        putIfPresent(out, "coinDiameter", existing.get("coinDiameter"));
        putIfPresent(out, "estimatedValue", existing.get("estimatedValue"));
        putIfPresent(out, "originCountry", existing.get("originCountry"));
        putIfPresent(out, "historicalSignificance", existing.get("historicalSignificance"));
        putIfPresent(out, "description", existing.get("description"));

        // collectionId may be present directly or inside a 'collection' nested object
        Object collId = existing.get("collectionId");
        if (collId == null) {
            Object coll = existing.get("collection");
            if (coll instanceof Map) {
                Object cid = ((Map<?,?>)coll).get("id");
                if (cid == null) cid = ((Map<?,?>)coll).get("collectionId");
                collId = cid;
            }
        }
        if (collId != null) out.put("collectionId", collId);
        return out;
    }

    private void putIfPresent(Map<String,Object> out, String key, Object val) {
        if (val != null) out.put(key, val);
    }

    // Helper: convert a string to Title Case (first letter of each word uppercase)
    private String toTitleCase(String s) {
        if (s == null || s.isEmpty()) return s;
        String[] parts = s.trim().toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i];
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0)));
            if (p.length() > 1) sb.append(p.substring(1));
            if (i < parts.length - 1) sb.append(' ');
        }
        return sb.toString();
    }

    private int parseStatusCode(String statusLine) {
        if (statusLine == null) return -1;
        String[] parts = statusLine.split(" ");
        if (parts.length < 2) return -1;
        try {
            return Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
