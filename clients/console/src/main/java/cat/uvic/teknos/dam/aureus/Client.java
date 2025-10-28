package cat.uvic.teknos.dam.aureus;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Scanner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Client {
    private final String host;
    private final int port;
    private final ObjectMapper mapper = new ObjectMapper();

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() {
        Scanner sc = new Scanner(System.in);

        System.out.println("+-----------------------------------------------+");
        System.out.println("|     AUREUS - Coin Management System Client    |");
        System.out.println("|     Connected to: " + host + ":" + port + "           |");
        System.out.println("+-----------------------------------------------+");

        while (true) {
            printMenu();
            System.out.print("Select an option: ");
            String option = sc.nextLine().trim();

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
                        System.out.println("\nGoodbye! Closing connection...");
                        return;
                    default:
                        System.out.println("\nInvalid option. Please try again.\n");
                }
            } catch (Exception e) {
                System.out.println("\nError: " + e.getMessage() + "\n");
            }

            waitForEnter(sc);
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("+-----------------------------------------------+");
        System.out.println("|              MAIN MENU - OPTIONS               |");
        System.out.println("+-----------------------------------------------+");
        System.out.println("|  1. List all coins                             |");
        System.out.println("|  2. Get coin by ID                             |");
        System.out.println("|  3. Create new coin                            |");
        System.out.println("|  4. Update coin                                |");
        System.out.println("|  5. Delete coin                                |");
        System.out.println("|  0. Exit                                       |");
        System.out.println("+-----------------------------------------------+");
    }

    private void listAllCoins() throws IOException {
        System.out.println("\nFetching all coins...\n");
        Response response = sendRequest("GET", "/coins", null);
        printFormattedResponse(response);
    }

    private void getCoinById(Scanner sc) throws IOException {
        // show available IDs before asking
        showAvailableIds();

        System.out.print("\nEnter coin ID: ");
        String id = sc.nextLine().trim();

        System.out.println("\nFetching coin with ID: " + id + "...\n");
        Response response = sendRequest("GET", "/coins/" + id, null);
        printFormattedResponse(response);
    }

    private void createCoin(Scanner sc) throws IOException {
        System.out.println("\nCREATE NEW COIN");
        System.out.println("---------------------------------");

        String name;
        while (true) {
            System.out.print("Coin name: ");
            name = sc.nextLine().trim();
            if (!name.isEmpty()) break;
            System.out.println("coinName is required. Please enter a name.");
        }

        String yearStr;
        while (true) {
            System.out.print("Coin year: ");
            yearStr = sc.nextLine().trim();
            try {
                Integer.parseInt(yearStr);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid year. Enter a valid integer (e.g. 1950 or -300).");
            }
        }

        // coinMaterial required
        String material;
        while (true) {
            System.out.print("Material: ");
            material = sc.nextLine().trim();
            if (!material.isEmpty()) break;
            System.out.println("coinMaterial is required. Please enter material.");
        }

        // originCountry required
        String country;
        while (true) {
            System.out.print("Origin country: ");
            country = sc.nextLine().trim();
            if (!country.isEmpty()) break;
            System.out.println("originCountry is required. Please enter origin country.");
        }

        // numeric fields: coinWeight, coinDiameter, estimatedValue
        String coinWeightStr;
        while (true) {
            System.out.print("Coin weight (numeric, e.g. 5.25): ");
            coinWeightStr = sc.nextLine().trim();
            try {
                Double.parseDouble(coinWeightStr);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }

        String coinDiameterStr;
        while (true) {
            System.out.print("Coin diameter (numeric, e.g. 21.5): ");
            coinDiameterStr = sc.nextLine().trim();
            try {
                Double.parseDouble(coinDiameterStr);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }

        String estimatedValueStr;
        while (true) {
            System.out.print("Estimated value (numeric, e.g. 150.00): ");
            estimatedValueStr = sc.nextLine().trim();
            try {
                Double.parseDouble(estimatedValueStr);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }

        // historicalSignificance (optional)
        System.out.print("Historical significance (optional, press Enter to skip): ");
        String historical = sc.nextLine().trim();

        // Show available collections and ask for collection id (required)
        Integer collectionId = promptForCollectionId(sc);

        // Build JSON body
        StringBuilder jsonBuilder = new StringBuilder("{");
        jsonBuilder.append("\"coinName\":\"").append(escapeJson(name)).append("\"");
        jsonBuilder.append(",\"coinYear\":").append(yearStr);
        jsonBuilder.append(",\"coinMaterial\":\"").append(escapeJson(material)).append("\"");
        jsonBuilder.append(",\"originCountry\":\"").append(escapeJson(country)).append("\"");
        jsonBuilder.append(",\"coinWeight\":").append(coinWeightStr);
        jsonBuilder.append(",\"coinDiameter\":").append(coinDiameterStr);
        jsonBuilder.append(",\"estimatedValue\":").append(estimatedValueStr);
        if (!historical.isEmpty()) {
            jsonBuilder.append(",\"historicalSignificance\":\"").append(escapeJson(historical)).append("\"");
        }
        // include collection id as nested object
        jsonBuilder.append(",\"collection\":{\"id\":").append(collectionId).append("}");
        jsonBuilder.append("}");

        System.out.println("\nSending request...\n");
        Response response = sendRequest("POST", "/coins", jsonBuilder.toString());
        System.out.println("Coin created (server response):");
        printFormattedResponse(response);
    }

    private Integer promptForCollectionId(Scanner sc) throws IOException {
        System.out.println("\nAvailable collections:");
        List<Integer> availableIds = new ArrayList<>();
        try {
            Response response = sendRequest("GET", "/collections", null);
            String body = response.body == null ? "" : response.body.trim();
            if (body.isEmpty()) {
                System.out.println("(no collections)");
                throw new IOException("No collections available on server");
            }
            List<Map<String,Object>> list = mapper.readValue(body, new TypeReference<>(){});
            List<String> headers = Arrays.asList("ID", "Name");
            List<List<String>> rows = new ArrayList<>();
            for (Map<String,Object> m : list) {
                Object id = m.get("id");
                Object name = m.get("collectionName");
                if (id instanceof Number) availableIds.add(((Number) id).intValue());
                else if (id != null) {
                    try { availableIds.add(Integer.parseInt(String.valueOf(id))); } catch (NumberFormatException ignored) {}
                }
                rows.add(Arrays.asList(id == null ? "" : String.valueOf(id), name == null ? "" : String.valueOf(name)));
            }
            printAsciiTable(headers, rows);
        } catch (Exception e) {
            System.out.println("Could not fetch collections: " + e.getMessage());
            throw new IOException("Cannot fetch collections", e);
        }

        while (true) {
            System.out.print("Enter collection ID to associate with the coin: ");
            String collStr = sc.nextLine().trim();
            try {
                int choice = Integer.parseInt(collStr);
                if (!availableIds.isEmpty() && !availableIds.contains(choice)) {
                    System.out.println("Invalid collection ID (not in the displayed list). Try again.");
                    continue;
                }
                return choice;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number, try again.");
            }
        }
    }

    private void updateCoin(Scanner sc) throws IOException {
        System.out.println("\nUPDATE COIN");
        System.out.println("---------------------------------");

        // show available IDs before asking
        showAvailableIds();

        System.out.print("Enter coin ID to update: ");
        String idStr = sc.nextLine().trim();

        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID. Must be an integer.");
            return;
        }

        // Verify coin exists
        Response checkResp = sendRequest("GET", "/coins/" + id, null);
        int status = 0;
        try {
            String[] parts = checkResp.statusLine.split(" ");
            if (parts.length >= 2) status = Integer.parseInt(parts[1]);
        } catch (Exception ignored) {}

        if (status == 404) {
            System.out.println("Coin with ID " + id + " not found. Aborting update.");
            return;
        }
        if (status >= 400) {
            System.out.println("Could not verify coin existence. Server returned: " + checkResp.statusLine);
            return;
        }

        // Now ask for all required fields (all NOT NULL except historicalSignificance)
        String name;
        while (true) {
            System.out.print("New coin name: ");
            name = sc.nextLine().trim();
            if (!name.isEmpty()) break;
            System.out.println("coinName is required. Please enter a name.");
        }

        String yearStr;
        while (true) {
            System.out.print("New coin year: ");
            yearStr = sc.nextLine().trim();
            try {
                Integer.parseInt(yearStr);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid year. Enter a valid integer (e.g. 1950 or -300).");
            }
        }

        String material;
        while (true) {
            System.out.print("New material: ");
            material = sc.nextLine().trim();
            if (!material.isEmpty()) break;
            System.out.println("coinMaterial is required. Please enter material.");
        }

        String country;
        while (true) {
            System.out.print("New origin country: ");
            country = sc.nextLine().trim();
            if (!country.isEmpty()) break;
            System.out.println("originCountry is required. Please enter origin country.");
        }

        String coinWeightStr;
        while (true) {
            System.out.print("New coin weight (numeric, e.g. 5.25): ");
            coinWeightStr = sc.nextLine().trim();
            try {
                Double.parseDouble(coinWeightStr);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }

        String coinDiameterStr;
        while (true) {
            System.out.print("New coin diameter (numeric, e.g. 21.5): ");
            coinDiameterStr = sc.nextLine().trim();
            try {
                Double.parseDouble(coinDiameterStr);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }

        String estimatedValueStr;
        while (true) {
            System.out.print("New estimated value (numeric, e.g. 150.00): ");
            estimatedValueStr = sc.nextLine().trim();
            try {
                Double.parseDouble(estimatedValueStr);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }

        // historicalSignificance (optional)
        System.out.print("Historical significance (optional, press Enter to skip): ");
        String historical = sc.nextLine().trim();

        // Show collections and ask for collection id
        Integer collectionId = promptForCollectionId(sc);

        // Build JSON body for PUT (include id and all fields)
        StringBuilder jsonBuilder = new StringBuilder("{");
        jsonBuilder.append("\"id\":").append(id);
        jsonBuilder.append(",\"coinName\":\"").append(escapeJson(name)).append("\"");
        jsonBuilder.append(",\"coinYear\":").append(yearStr);
        jsonBuilder.append(",\"coinMaterial\":\"").append(escapeJson(material)).append("\"");
        jsonBuilder.append(",\"originCountry\":\"").append(escapeJson(country)).append("\"");
        jsonBuilder.append(",\"coinWeight\":").append(coinWeightStr);
        jsonBuilder.append(",\"coinDiameter\":").append(coinDiameterStr);
        jsonBuilder.append(",\"estimatedValue\":").append(estimatedValueStr);
        if (!historical.isEmpty()) {
            jsonBuilder.append(",\"historicalSignificance\":\"").append(escapeJson(historical)).append("\"");
        }
        jsonBuilder.append(",\"collection\":{\"id\":").append(collectionId).append("}");
        jsonBuilder.append("}");

        System.out.println("\nSending request...\n");
        Response response = sendRequest("PUT", "/coins/" + id, jsonBuilder.toString());
        System.out.println("Coin update response:");
        printFormattedResponse(response);
    }

    private void deleteCoin(Scanner sc) throws IOException {
        System.out.println("\nDELETE COIN");
        System.out.println("---------------------------------");

        // show available IDs before asking
        showAvailableIds();

        System.out.print("Enter coin ID to delete: ");
        String id = sc.nextLine().trim();

        // Validate id is an integer
        int idInt;
        try {
            idInt = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID. Must be an integer.");
            return;
        }

        // Check existence before asking for confirmation
        Response checkResp = sendRequest("GET", "/coins/" + idInt, null);
        int status = 0;
        try {
            String[] parts = checkResp.statusLine.split(" ");
            if (parts.length >= 2) status = Integer.parseInt(parts[1]);
        } catch (Exception ignored) {}

        if (status == 404) {
            System.out.println("Coin with ID " + idInt + " not found. Aborting delete.");
            return;
        }
        if (status >= 400) {
            System.out.println("Could not verify coin existence. Server returned: " + checkResp.statusLine);
            return;
        }

        System.out.print("Are you sure you want to delete coin #" + id + "? (yes/no): ");
        String confirm = sc.nextLine().trim();

        if (confirm.equalsIgnoreCase("yes") || confirm.equalsIgnoreCase("y")) {
            System.out.println("\nSending request...\n");
            Response response = sendRequest("DELETE", "/coins/" + idInt, null);
            System.out.println("Coin delete response:");
            printFormattedResponse(response);
        } else {
            System.out.println("\nDelete operation cancelled.");
        }
    }

    private void waitForEnter(Scanner sc) {
        System.out.print("\nPress Enter to continue...");
        sc.nextLine();
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

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
                // Use bytes length for Content-Length
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
            return new Response(statusLine, bodyResp);
        }
    }

    private void printFormattedResponse(Response response) {
        System.out.println(response.statusLine);
        String body = response.body == null ? "" : response.body.trim();
        if (body.isEmpty()) {
            System.out.println("(no body)");
            return;
        }

        // Try to parse JSON
        try {
            if (body.startsWith("[")) {
                List<Map<String, Object>> list = mapper.readValue(body, new TypeReference<>(){});
                if (list.isEmpty()) {
                    System.out.println("[]");
                    return;
                }
                // Collect keys in deterministic order (first appearance)
                LinkedHashSet<String> keys = new LinkedHashSet<>();
                for (Map<String, Object> m : list) keys.addAll(m.keySet());

                // Build headers and rows
                List<String> headers = new ArrayList<>(keys);
                List<List<String>> rowsData = new ArrayList<>();
                for (Map<String, Object> m : list) {
                    List<String> row = new ArrayList<>();
                    for (String h : headers) row.add(m.get(h) == null ? "" : String.valueOf(m.get(h)));
                    rowsData.add(row);
                }
                printAsciiTable(headers, rowsData);
                return;
            } else if (body.startsWith("{")) {
                Map<String, Object> map = mapper.readValue(body, new TypeReference<>(){});
                // Show as a table with keys as columns and a single row
                LinkedHashSet<String> keys = new LinkedHashSet<>(map.keySet());

                List<String> headers = new ArrayList<>(keys);
                List<List<String>> rowsData = new ArrayList<>();
                List<String> single = new ArrayList<>();
                for (String h : headers) single.add(map.get(h) == null ? "" : String.valueOf(map.get(h)));
                rowsData.add(single);
                printAsciiTable(headers, rowsData);
                return;
            }
        } catch (Exception e) {
            // If parsing fails, fall through to raw print
        }

        // Not JSON or failed to parse: print raw body
        System.out.println(body);
    }

    private static class Response {
        public final String statusLine;
        public final String body;

        public Response(String statusLine, String body) {
            this.statusLine = statusLine;
            this.body = body;
        }
    }

    // Local ASCII table printer (simple, similar style to app's tables)
    private void printAsciiTable(List<String> headers, List<List<String>> rows) {
        final int MAX_COL_WIDTH = 30; // cap column width to avoid very wide tables

        int cols = headers.size();
        int[] widths = new int[cols];

        // initial widths from headers
        for (int i = 0; i < cols; i++) widths[i] = Math.min(headers.get(i) == null ? 0 : headers.get(i).length(), MAX_COL_WIDTH);

        // expand with cell contents (capped)
        for (List<String> r : rows) {
            for (int i = 0; i < cols; i++) {
                String cell = i < r.size() && r.get(i) != null ? r.get(i) : "";
                widths[i] = Math.max(widths[i], Math.min(cell.length(), MAX_COL_WIDTH));
            }
        }

        // build separator line
        StringBuilder sep = new StringBuilder("+");
        for (int w : widths) {
            sep.append(String.join("", Collections.nCopies(w + 2, "-"))).append("+");
        }

        // header
        System.out.println(sep.toString());
        StringBuilder headerLine = new StringBuilder("|");
        for (int i = 0; i < cols; i++) {
            headerLine.append(' ').append(formatCell(headers.get(i), widths[i], false)).append(' ').append('|');
        }
        System.out.println(headerLine.toString());

        // header separator (different char for clarity)
        StringBuilder headerSep = new StringBuilder("+");
        for (int w : widths) {
            headerSep.append(String.join("", Collections.nCopies(w + 2, "="))).append("+");
        }
        System.out.println(headerSep.toString());

        // rows
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
        // truncate if necessary
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

    // Show a simple list of available IDs and coin names (ID | Name)
    private void showAvailableIds() {
        try {
            Response response = sendRequest("GET", "/coins", null);
            String body = response.body == null ? "" : response.body.trim();
            if (body.isEmpty()) {
                System.out.println("(no coins)");
                return;
            }
            if (!body.startsWith("[")) {
                // not an array, try parsing as single object
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
}
