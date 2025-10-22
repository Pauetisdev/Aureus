package cat.uvic.teknos.dam.aureus;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {
    private final String host;
    private final int port;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() {
        Scanner sc = new Scanner(System.in);

        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║     AUREUS - Coin Management System Client    ║");
        System.out.println("║            Connected to: " + host + ":" + port + "            ║");
        System.out.println("╚════════════════════════════════════════════════╝");

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
                        System.out.println("\n👋 Goodbye! Closing connection...");
                        return;
                    default:
                        System.out.println("\n❌ Invalid option. Please try again.\n");
                }
            } catch (Exception e) {
                System.out.println("\n❌ Error: " + e.getMessage() + "\n");
            }

            waitForEnter(sc);
        }
    }

    private void printMenu() {
        System.out.println("\n┌────────────────────────────────────────────────┐");
        System.out.println("│              MAIN MENU - OPTIONS               │");
        System.out.println("├────────────────────────────────────────────────┤");
        System.out.println("│  1. List all coins                             │");
        System.out.println("│  2. Get coin by ID                             │");
        System.out.println("│  3. Create new coin                            │");
        System.out.println("│  4. Update coin                                │");
        System.out.println("│  5. Delete coin                                │");
        System.out.println("│  0. Exit                                       │");
        System.out.println("└────────────────────────────────────────────────┘");
    }

    private void listAllCoins() throws IOException {
        System.out.println("\n📋 Fetching all coins...\n");
        String response = sendRequest("GET", "/coins", null);
        System.out.println("Response:");
        System.out.println(response);
    }

    private void getCoinById(Scanner sc) throws IOException {
        System.out.print("\nEnter coin ID: ");
        String id = sc.nextLine().trim();

        System.out.println("\n🔍 Fetching coin with ID: " + id + "...\n");
        String response = sendRequest("GET", "/coins/" + id, null);
        System.out.println("Response:");
        System.out.println(response);
    }

    private void createCoin(Scanner sc) throws IOException {
        System.out.println("\n➕ CREATE NEW COIN");
        System.out.println("─────────────────────────────────────────");

        System.out.print("Coin name: ");
        String name = sc.nextLine().trim();

        System.out.print("Coin year: ");
        String yearStr = sc.nextLine().trim();

        System.out.print("Material (optional, press Enter to skip): ");
        String material = sc.nextLine().trim();

        System.out.print("Origin country (optional, press Enter to skip): ");
        String country = sc.nextLine().trim();

        // Build JSON body
        StringBuilder jsonBuilder = new StringBuilder("{");
        jsonBuilder.append("\"coinName\":\"").append(name).append("\"");
        jsonBuilder.append(",\"coinYear\":").append(yearStr);

        if (!material.isEmpty()) {
            jsonBuilder.append(",\"coinMaterial\":\"").append(material).append("\"");
        }
        if (!country.isEmpty()) {
            jsonBuilder.append(",\"originCountry\":\"").append(country).append("\"");
        }
        jsonBuilder.append("}");

        System.out.println("\n📤 Sending request...\n");
        String response = sendRequest("POST", "/coins", jsonBuilder.toString());
        System.out.println("✅ Coin created successfully!");
        System.out.println("Response:");
        System.out.println(response);
    }

    private void updateCoin(Scanner sc) throws IOException {
        System.out.println("\n✏️  UPDATE COIN");
        System.out.println("─────────────────────────────────────────");

        System.out.print("Enter coin ID to update: ");
        String id = sc.nextLine().trim();

        System.out.print("New coin name: ");
        String name = sc.nextLine().trim();

        System.out.print("New coin year: ");
        String yearStr = sc.nextLine().trim();

        System.out.print("New material (optional, press Enter to skip): ");
        String material = sc.nextLine().trim();

        System.out.print("New origin country (optional, press Enter to skip): ");
        String country = sc.nextLine().trim();

        // Build JSON body
        StringBuilder jsonBuilder = new StringBuilder("{");
        jsonBuilder.append("\"id\":").append(id);
        jsonBuilder.append(",\"coinName\":\"").append(name).append("\"");
        jsonBuilder.append(",\"coinYear\":").append(yearStr);

        if (!material.isEmpty()) {
            jsonBuilder.append(",\"coinMaterial\":\"").append(material).append("\"");
        }
        if (!country.isEmpty()) {
            jsonBuilder.append(",\"originCountry\":\"").append(country).append("\"");
        }
        jsonBuilder.append("}");

        System.out.println("\n📤 Sending request...\n");
        String response = sendRequest("PUT", "/coins/" + id, jsonBuilder.toString());
        System.out.println("✅ Coin updated successfully!");
        System.out.println("Response:");
        System.out.println(response);
    }

    private void deleteCoin(Scanner sc) throws IOException {
        System.out.println("\n🗑️  DELETE COIN");
        System.out.println("─────────────────────────────────────────");

        System.out.print("Enter coin ID to delete: ");
        String id = sc.nextLine().trim();

        System.out.print("Are you sure you want to delete coin #" + id + "? (yes/no): ");
        String confirm = sc.nextLine().trim();

        if (confirm.equalsIgnoreCase("yes") || confirm.equalsIgnoreCase("y")) {
            System.out.println("\n📤 Sending request...\n");
            String response = sendRequest("DELETE", "/coins/" + id, null);
            System.out.println("✅ Coin deleted successfully!");
            System.out.println("Response:");
            System.out.println(response);
        } else {
            System.out.println("\n❌ Delete operation cancelled.");
        }
    }

    private void waitForEnter(Scanner sc) {
        System.out.print("\nPress Enter to continue...");
        sc.nextLine();
    }

    private String sendRequest(String method, String path, String body) throws IOException {
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
                reqOut.write(body.getBytes(StandardCharsets.UTF_8));
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
            return statusLine + "\n" + bodyResp;
        }
    }
}
