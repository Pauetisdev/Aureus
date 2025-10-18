package cat.uvic.teknos.dam.aureus;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {
    private final String host;
    private final int port;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() {
        System.out.println("Simple Console HTTP Client. Commands: list, get <id>, create <name> <year>, update <id> <name> <year>, delete <id>, exit");
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String line = sc.nextLine();
            if (line == null) break;
            line = line.trim();
            if (line.equalsIgnoreCase("exit")) break;
            try {
                if (line.equalsIgnoreCase("list")) {
                    var resp = sendRequest("GET", "/coins", null);
                    System.out.println(resp);
                } else if (line.startsWith("get ")) {
                    String[] parts = line.split(" ", 2);
                    String id = parts[1].trim();
                    var resp = sendRequest("GET", "/coins/" + id, null);
                    System.out.println(resp);
                } else if (line.startsWith("create ")) {
                    // create Name Year
                    Matcher m = Pattern.compile("create\\s+([^\\s]+)\\s+(-?\\d+)").matcher(line);
                    if (m.matches()) {
                        String name = m.group(1);
                        String year = m.group(2);
                        String body = String.format("{\"coinName\":\"%s\",\"coinYear\":%s}", name, year);
                        var resp = sendRequest("POST", "/coins", body);
                        System.out.println(resp);
                    } else {
                        System.out.println("Usage: create <name> <year>");
                    }
                } else if (line.startsWith("update ")) {
                    // update id Name Year
                    Matcher m = Pattern.compile("update\\s+(\\d+)\\s+([^\\s]+)\\s+(-?\\d+)").matcher(line);
                    if (m.matches()) {
                        String id = m.group(1);
                        String name = m.group(2);
                        String year = m.group(3);
                        String body = String.format("{\"id\":%s,\"coinName\":\"%s\",\"coinYear\":%s}", id, name, year);
                        var resp = sendRequest("PUT", "/coins/" + id, body);
                        System.out.println(resp);
                    } else {
                        System.out.println("Usage: update <id> <name> <year>");
                    }
                } else if (line.startsWith("delete ")) {
                    String[] parts = line.split(" ", 2);
                    String id = parts[1].trim();
                    var resp = sendRequest("DELETE", "/coins/" + id, null);
                    System.out.println(resp);
                } else {
                    System.out.println("Unknown command.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        System.out.println("Bye");
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
