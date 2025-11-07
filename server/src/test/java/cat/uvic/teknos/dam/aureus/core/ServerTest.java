package cat.uvic.teknos.dam.aureus.core;

import cat.uvic.teknos.dam.aureus.http.RequestRouter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ServerTest {

    @Test
    void startHandlesOneClientAndShutsDown() throws Exception {
        // Mock router: read input and write simple response
        RequestRouter router = Mockito.mock(RequestRouter.class);
        // When router.handleRequest is called, write a minimal HTTP response to the output stream
        doAnswer(invocation -> {
            InputStream in = invocation.getArgument(0, InputStream.class);
            OutputStream out = invocation.getArgument(1, OutputStream.class);
            // Write a small HTTP response
            String resp = "HTTP/1.1 200 OK\r\nContent-Length: 2\r\n\r\nOK";
            out.write(resp.getBytes(StandardCharsets.UTF_8));
            out.flush();
            return null;
        }).when(router).handleRequest(any(InputStream.class), any(OutputStream.class));

        // Fake ServerSocket that accepts once and then throws IOException
        class OneShotServerSocket extends ServerSocket {
            private boolean served = false;

            protected OneShotServerSocket() throws IOException {
                super(); // protected ctor
            }

            @Override
            public Socket accept() throws IOException {
                if (served) throw new IOException("no more accepts");
                served = true;
                return new Socket() {
                    private final InputStream in = new ByteArrayInputStream("GET /coins HTTP/1.1\r\nHost: x\r\n\r\n".getBytes(StandardCharsets.UTF_8));
                    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

                    @Override
                    public InputStream getInputStream() {
                        return in;
                    }

                    @Override
                    public OutputStream getOutputStream() {
                        return out;
                    }

                    @Override
                    public synchronized void close() {
                        // no-op to avoid closing underlying streams in test
                    }
                };
            }
        }

        ServerSocket fakeSocket = new OneShotServerSocket();
        Server server = new Server(0, router, fakeSocket);

        // start should return normally after one client + subsequent IOException
        server.start();

        // Verify router was invoked
        verify(router, atLeastOnce()).handleRequest(any(InputStream.class), any(OutputStream.class));

        // Reflectively check that connectedClients counter returned to 0
        Field connectedField = Server.class.getDeclaredField("connectedClients");
        connectedField.setAccessible(true);
        AtomicInteger connected = (AtomicInteger) connectedField.get(server);
        assertNotNull(connected, "connectedClients field should be present");
        assertEquals(0, connected.get(), "After shutdown there should be no connected clients");
    }
}
