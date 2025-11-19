package cat.uvic.teknos.dam.aureus.http;

import cat.uvic.teknos.dam.aureus.controller.CoinController;
import cat.uvic.teknos.dam.aureus.security.CryptoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class RequestRouterHashTest {

    private CoinController controllerMock;
    private RequestRouter router;

    @BeforeEach
    void setUp() {
        controllerMock = Mockito.mock(CoinController.class);
        when(controllerMock.createCoin(Mockito.anyString())).thenReturn("{\"created\":true}");
        router = new RequestRouter(controllerMock);
    }

    @Test
    void acceptsRequestWithValidBodyHash() throws Exception {
        String body = "{\"name\":\"Test Coin\"}";
        String hash = CryptoUtils.hash(body);
        String req = "POST /coins HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                "X-Body-Hash: " + hash + "\r\n" +
                "\r\n" +
                body;

        ByteArrayInputStream in = new ByteArrayInputStream(req.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        router.handleRequest(in, out);

        String response = out.toString(StandardCharsets.UTF_8.name());
        assertTrue(response.startsWith("HTTP/1.1 201"));
        assertTrue(response.contains("X-Body-Hash:"));
    }

    @Test
    void rejectsRequestWithInvalidBodyHash() throws Exception {
        String body = "{\"name\":\"Test Coin\"}";
        String badHash = "deadbeef";
        String req = "POST /coins HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                "X-Body-Hash: " + badHash + "\r\n" +
                "\r\n" +
                body;

        ByteArrayInputStream in = new ByteArrayInputStream(req.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        router.handleRequest(in, out);

        String response = out.toString(StandardCharsets.UTF_8.name());
        // Should be 400 Bad Request
        assertTrue(response.startsWith("HTTP/1.1 400"));
        assertTrue(response.contains("Invalid or missing body hash"));
    }
}

