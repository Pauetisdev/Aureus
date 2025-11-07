package cat.uvic.teknos.dam.aureus.http;

import cat.uvic.teknos.dam.aureus.controller.CoinController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequestRouterTest {

    private CoinController controllerMock;
    private RequestRouter router;

    @BeforeEach
    void setUp() {
        controllerMock = Mockito.mock(CoinController.class);
        when(controllerMock.getAllCoins()).thenReturn("[]");
        when(controllerMock.getCoin(1)).thenReturn("{\"id\":1}");
        router = new RequestRouter(controllerMock);
    }

    @Test
    void routeReturns200ForGetAllCoins() {
        HttpRequest req = new HttpRequest("GET", "/coins", new HashMap<>(), "");
        ResponseEntity res = router.route(req);
        assertEquals(200, res.getStatus());
        String body = new String(res.getBody() == null ? new byte[0] : res.getBody());
        assertTrue(body.contains("[]"));
        verify(controllerMock).getAllCoins();
    }

    @Test
    void routeReturns200ForGetCoinById() {
        HttpRequest req = new HttpRequest("GET", "/coins/1", new HashMap<>(), "");
        ResponseEntity res = router.route(req);
        assertEquals(200, res.getStatus());
        String body = new String(res.getBody() == null ? new byte[0] : res.getBody());
        assertTrue(body.contains("\"id\":1"));
        verify(controllerMock).getCoin(1);
    }

    @Test
    void routeUnknownPathThrowsHttpException() {
        HttpRequest req = new HttpRequest("GET", "/unknown", new HashMap<>(), "");
        RuntimeException ex = assertThrows(RuntimeException.class, () -> router.route(req));
        // Ensure it's the HttpException or a subclass (message typically contains "Resource not found")
        assertTrue(ex.getClass().getName().contains("HttpException") || ex.getMessage().toLowerCase().contains("not found"));
    }

    @Test
    void routeDisconnectReturnsAck() {
        HttpRequest req = new HttpRequest("GET", "/disconnect", new HashMap<>(), "");
        ResponseEntity res = router.route(req);
        assertEquals(200, res.getStatus());
        String body = new String(res.getBody() == null ? new byte[0] : res.getBody());
        assertEquals(RequestRouter.DISCONNECT_ACK_BODY, body);
    }
}
