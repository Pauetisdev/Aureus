package cat.uvic.teknos.dam.aureus.http;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class HttpRequestTest {

    @Test
    void parseGetWithoutBody() throws Exception {
        String raw = "GET /coins HTTP/1.1\r\nHost: localhost\r\n\r\n";
        ByteArrayInputStream in = new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8));
        HttpRequest req = HttpRequest.parse(in);
        assertEquals("GET", req.getMethod());
        assertEquals("/coins", req.getPath());
        assertEquals("", req.getBody());
    }

    @Test
    void parsePostWithContentLengthBody() throws Exception {
        String body = "{\"coinName\":\"X\"}";
        String raw = "POST /coins HTTP/1.1\r\nHost: localhost\r\nContent-Length: " + body.length() + "\r\n\r\n" + body;
        ByteArrayInputStream in = new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8));
        HttpRequest req = HttpRequest.parse(in);
        assertEquals("POST", req.getMethod());
        assertEquals("/coins", req.getPath());
        assertEquals(body, req.getBody());
    }
}

