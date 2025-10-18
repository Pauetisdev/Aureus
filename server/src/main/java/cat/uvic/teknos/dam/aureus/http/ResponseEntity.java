package cat.uvic.teknos.dam.aureus.http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class ResponseEntity {
    private final int status;
    private final String reason;
    private final Map<String, String> headers = new LinkedHashMap<>();
    private final byte[] body;

    public ResponseEntity(int status, String reason, Map<String, String> headers, byte[] body) {
        this.status = status;
        this.reason = reason;
        if (headers != null) this.headers.putAll(headers);
        this.body = body == null ? new byte[0] : body;
    }

    public int getStatus() { return status; }
    public String getReason() { return reason; }
    public Map<String,String> getHeaders() { return headers; }
    public byte[] getBody() { return body; }

    public void writeTo(OutputStream out) throws IOException {
        String statusLine = String.format("HTTP/1.1 %d %s\r\n", status, reason);
        out.write(statusLine.getBytes(StandardCharsets.UTF_8));

        if (!headers.containsKey("Content-Length")) {
            headers.put("Content-Length", String.valueOf(body.length));
        }
        for (Map.Entry<String, String> h : headers.entrySet()) {
            String line = h.getKey() + ": " + h.getValue() + "\r\n";
            out.write(line.getBytes(StandardCharsets.UTF_8));
        }
        out.write("\r\n".getBytes(StandardCharsets.UTF_8));
        if (body.length > 0) out.write(body);
        out.flush();
    }
}

