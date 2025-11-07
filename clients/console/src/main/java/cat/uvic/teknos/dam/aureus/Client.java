package cat.uvic.teknos.dam.aureus;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private final String host;
    private final int port;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<Integer, String> collectionNameCache = new HashMap<>();

    // Marca de tiempo de la última actividad (lectura/entrada del usuario/operación de E/S)
    private volatile long lastActivityTime = System.currentTimeMillis();
    // Scheduler para monitorizar inactividad
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    // Timeout de inactividad configurable (2 minutos por requerimiento)
    private final long INACTIVITY_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(2);
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
                } finally {
                    System.out.println("Exiting client application.");
                    shutdown();
                    System.exit(0);
                }
            }
        }
    }

    // Maneja la salida explícita desde el menú (envía DISCONNECT antes de cerrar)
    private void handleGracefulExit() throws IOException {
        if (running.compareAndSet(true, false)) {
            System.out.println("\nGoodbye! Closing connection...");
            try {
                System.out.println("Sending disconnect signal to server...");
                sendRequest("GET", DISCONNECT_PATH, null);
            } catch (IOException e) {
                // ignorar errores al notificar
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
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                String[] headerParts = line.split(":", 2);
                if (headerParts.length == 2) {
                    String name = headerParts[0].trim();
                    String value = headerParts[1].trim();
                    if (name.equalsIgnoreCase("Content-Length")) {
                        try { contentLength = Integer.parseInt(value); } catch (NumberFormatException ignored) {}
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

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void printFormattedResponse(Response response) {
        System.out.println(response.statusLine);
        String body = response.body == null ? "" : response.body.trim();
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
                printAsciiTable(headers, rowsData);
                return;
            } else if (body.startsWith("{")) {
                Map<String, Object> map = mapper.readValue(body, new TypeReference<>(){});
                LinkedHashSet<String> keys = new LinkedHashSet<>(map.keySet());

                List<String> headers = new ArrayList<>(keys);
                List<List<String>> rowsData = new ArrayList<>();
                List<String> single = new ArrayList<>();
                for (String h : headers) {
                    Object raw = map.get(h);
                    if ("collection".equalsIgnoreCase(h)) single.add(formatCollectionValue(raw));
                    else single.add(formatValue(raw));
                }
                rowsData.add(single);
                printAsciiTable(headers, rowsData);
                return;
            }
        } catch (Exception e) {

        }

        System.out.println(body);
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
            System.out.print("Enter coin ID: ");
            String id = sc.nextLine().trim();
            if (id.isEmpty()) {
                System.out.println("ID required.");
                return;
            }
            Response r = sendRequest("GET", "/coins/" + id, null);
            printFormattedResponse(r);
        } catch (Exception e) {
            System.out.println("Error fetching coin: " + e.getMessage());
        }
    }

    private void createCoin(Scanner sc) {
        try {
            System.out.print("coinName: ");
            String name = sc.nextLine().trim();
            System.out.print("year (numeric): ");
            String yearStr = sc.nextLine().trim();
            System.out.print("collectionId (numeric): ");
            String collStr = sc.nextLine().trim();

            Map<String,Object> payload = new LinkedHashMap<>();
            if (!name.isEmpty()) payload.put("coinName", name);
            if (!yearStr.isEmpty()) {
                try { payload.put("year", Integer.parseInt(yearStr)); } catch (NumberFormatException ignored) {}
            }
            if (!collStr.isEmpty()) {
                try { payload.put("collectionId", Integer.parseInt(collStr)); } catch (NumberFormatException ignored) {}
            }
            String body = mapper.writeValueAsString(payload);
            Response r = sendRequest("POST", "/coins", body);
            printFormattedResponse(r);
        } catch (Exception e) {
            System.out.println("Error creating coin: " + e.getMessage());
        }
    }

    private void updateCoin(Scanner sc) {
        try {
            System.out.print("Enter coin ID to update: ");
            String id = sc.nextLine().trim();
            if (id.isEmpty()) {
                System.out.println("ID required.");
                return;
            }
            System.out.print("coinName (leave blank to keep): ");
            String name = sc.nextLine().trim();
            System.out.print("year (leave blank to keep): ");
            String yearStr = sc.nextLine().trim();
            System.out.print("collectionId (leave blank to keep): ");
            String collStr = sc.nextLine().trim();

            Map<String,Object> payload = new LinkedHashMap<>();
            if (!name.isEmpty()) payload.put("coinName", name);
            if (!yearStr.isEmpty()) {
                try { payload.put("year", Integer.parseInt(yearStr)); } catch (NumberFormatException ignored) {}
            }
            if (!collStr.isEmpty()) {
                try { payload.put("collectionId", Integer.parseInt(collStr)); } catch (NumberFormatException ignored) {}
            }

            if (payload.isEmpty()) {
                System.out.println("No updates provided.");
                return;
            }

            String body = mapper.writeValueAsString(payload);
            Response r = sendRequest("PUT", "/coins/" + id, body);
            printFormattedResponse(r);
        } catch (Exception e) {
            System.out.println("Error updating coin: " + e.getMessage());
        }
    }

    private void deleteCoin(Scanner sc) {
        try {
            System.out.print("Enter coin ID to delete: ");
            String id = sc.nextLine().trim();
            if (id.isEmpty()) {
                System.out.println("ID required.");
                return;
            }
            Response r = sendRequest("DELETE", "/coins/" + id, null);
            printFormattedResponse(r);
        } catch (Exception e) {
            System.out.println("Error deleting coin: " + e.getMessage());
        }
    }
}
