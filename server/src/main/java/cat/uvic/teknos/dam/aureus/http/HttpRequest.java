package cat.uvic.teknos.dam.aureus.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a parsed HTTP request with method, path, headers and body.
 *
 * <p>The {@link #parse(InputStream)} method parses a raw HTTP request from
 * an InputStream and returns a populated {@code HttpRequest} instance.</p>
 */
public class HttpRequest {
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final String body;

    public HttpRequest(String method, String path, Map<String, String> headers, String body) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
    }

    public String getMethod() { return method; }
    public String getPath() { return path; }
    public Map<String,String> getHeaders() { return headers; }
    public String getBody() { return body; }

    /**
     * Parse a raw HTTP request from the provided InputStream.
     *
     * <p>The method reads headers until the CRLFCRLF sequence and then
     * reads the body according to the Content-Length header (if present).</p>
     *
     * @param input input stream containing the raw HTTP request
     * @return parsed {@link HttpRequest}
     * @throws IOException on I/O or protocol parsing errors
     */
    public static HttpRequest parse(InputStream input) throws IOException {
        // Read bytes until the sequence CRLF CRLF is found, which marks the end of headers
        ByteArrayOutputStream headerBuffer = new ByteArrayOutputStream();
        int b;
        // We'll keep a sliding window of the last 4 bytes to detect \r\n\r\n
        byte[] last4 = new byte[4];
        int lastCount = 0;
        while ((b = input.read()) != -1) {
            headerBuffer.write(b);
            // shift last4
            if (lastCount < 4) {
                last4[lastCount++] = (byte) b;
            } else {
                last4[0] = last4[1];
                last4[1] = last4[2];
                last4[2] = last4[3];
                last4[3] = (byte) b;
            }
            if (lastCount == 4 && last4[0] == '\r' && last4[1] == '\n' && last4[2] == '\r' && last4[3] == '\n') {
                break;
            }
        }

        if (headerBuffer.size() == 0) {
            throw new IOException("Empty request");
        }

        byte[] headerBytes = headerBuffer.toByteArray();
        // find header end index (position of the \r\n\r\n)
        int headerEnd = -1;
        for (int i = 0; i < headerBytes.length - 3; i++) {
            if (headerBytes[i] == '\r' && headerBytes[i+1] == '\n' && headerBytes[i+2] == '\r' && headerBytes[i+3] == '\n') {
                headerEnd = i;
                break;
            }
        }
        if (headerEnd == -1) {
            throw new IOException("Invalid HTTP header: no header-body separator");
        }

        String headerStr = new String(headerBytes, 0, headerEnd, StandardCharsets.UTF_8);
        String[] lines = headerStr.split("\r\n");
        if (lines.length == 0) throw new IOException("Invalid request line");
        String requestLine = lines[0];
        String[] parts = requestLine.split(" ", 3);
        if (parts.length < 2) throw new IOException("Invalid request line: " + requestLine);
        String method = parts[0];
        String path = parts[1];

        Map<String, String> headers = new HashMap<>();
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line == null || line.isEmpty()) continue;
            int idx = line.indexOf(':');
            if (idx > 0) {
                String name = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();
                headers.put(name, value);
            }
        }

        // Determine content length (default 0)
        String contentLengthValue = headers.getOrDefault("Content-Length", headers.getOrDefault("content-length", "0"));
        int contentLength = 0;
        try { contentLength = Integer.parseInt(contentLengthValue); } catch (NumberFormatException ignored) {}

        // Compute any body bytes already read after the header end
        int alreadyRead = headerBytes.length - (headerEnd + 4);
        ByteArrayOutputStream bodyOut = new ByteArrayOutputStream();
        if (alreadyRead > 0) {
            bodyOut.write(headerBytes, headerEnd + 4, alreadyRead);
        }

        // Read remaining bytes if any
        while (bodyOut.size() < contentLength) {
            int toRead = contentLength - bodyOut.size();
            byte[] buf = new byte[Math.min(1024, toRead)];
            int n = input.read(buf);
            if (n == -1) break;
            bodyOut.write(buf, 0, n);
        }

        String body = bodyOut.size() == 0 ? "" : new String(bodyOut.toByteArray(), StandardCharsets.UTF_8);
        return new HttpRequest(method, path, headers, body);
    }
}
