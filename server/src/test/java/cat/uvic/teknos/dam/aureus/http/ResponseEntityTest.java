package cat.uvic.teknos.dam.aureus.http;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class ResponseEntityTest {

    @Test
    void writeToProducesValidHttpResponseWithBody() throws Exception {
        byte[] body = "{\"ok\":true}".getBytes(StandardCharsets.UTF_8);
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        ResponseEntity res = new ResponseEntity(200, "OK", headers, body);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        res.writeTo(out);

        String raw = out.toString(StandardCharsets.UTF_8.name());
        assertTrue(raw.startsWith("HTTP/1.1 200 OK"));
        assertTrue(raw.contains("Content-Type: application/json"));
        assertTrue(raw.endsWith(new String(body, StandardCharsets.UTF_8)));
    }
}

