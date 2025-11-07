package cat.uvic.teknos.dam.aureus.core;

import cat.uvic.teknos.dam.aureus.http.RequestRouter;
import cat.uvic.teknos.dam.aureus.http.ResponseEntity;


import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ThreadFactory;

/**
 * Multi-threaded HTTP server used by the project for testing and demo.
 *
 * <p>The server accepts TCP connections, delegates request handling to the
 * provided {@link RequestRouter} and writes responses back to the client.
 * It uses a thread pool for concurrent client handling and a daemon thread
 * for status reporting.</p>
 */
public class Server {

    private final int port;
    private final RequestRouter router;
    private ServerSocket serverSocket;

    // Componentes de concurrencia
    private final ExecutorService clientExecutor;
    private final ScheduledExecutorService scheduler;
    private final AtomicInteger connectedClients; // Contador atómico para clientes conectados

    // Contador para dar nombre a los hilos del pool
    private final AtomicInteger clientThreadCounter = new AtomicInteger(1);

    public Server(int port, RequestRouter router) {
        this.port = port;
        this.router = router;
        // Usar un pool cached con ThreadFactory para nombrar hilos de cliente
        this.clientExecutor = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "ClientHandler-" + clientThreadCounter.getAndIncrement());
                t.setDaemon(false); // Hilos de manejo de cliente NO deben ser daemons
                return t;
            }
        });
        // Usar un scheduler de un solo hilo para la tarea daemon
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ServerStatusDaemon");
            t.setDaemon(true); // Debe ser un hilo daemon
            return t;
        });
        this.connectedClients = new AtomicInteger(0);
    }

    // Nuevo constructor: para test
    public Server(int port, RequestRouter router, ServerSocket serverSocket) {
        this(port, router); // Delegar al constructor principal para configurar la concurrencia
        this.serverSocket = serverSocket;
    }

    /**
     * Start accepting incoming connections and process them concurrently.
     * Starts the status daemon thread.
     * The method blocks until the server is shut down or an unrecoverable error occurs.
     */
    public void start() {
        try {
            if (serverSocket == null) {
                serverSocket = new ServerSocket(port);
                System.out.println("Server started and listening on port: " + port);
            } else {
                System.out.println("Server started with provided ServerSocket on port: " + port);
            }

            // Iniciar hilo daemon que imprime el número de clientes conectados cada minuto
            scheduler.scheduleAtFixedRate(this::printStatus, 1, 1, TimeUnit.MINUTES);
            System.out.println("Status daemon started, reporting connected clients every minute.");

            while (!serverSocket.isClosed()) {
                // 1. ACEPTAR: Bloquea hasta que un nuevo cliente se conecta.
                Socket clientSocket = serverSocket.accept();

                // 2. PROCESAR: Enviar la tarea a un Thread Pool.
                clientExecutor.submit(() -> handleClientConnection(clientSocket));
            }
        } catch (BindException e) {
            System.err.println("Port " + port + " is already in use. " + e.getMessage());
        } catch (IOException e) {
            // Maneja el error que ocurre típicamente al cerrar el ServerSocket
            if (serverSocket != null && serverSocket.isClosed()) {
                System.out.println("Server stopped.");
            } else {
                System.err.println("Error starting or running ServerSocket: " + e.getMessage());
            }
        } finally {
            shutdown();
        }
    }

    // Método que imprime el estado (número de clientes conectados)
    private void printStatus() {
        System.out.println("STATUS: Currently connected clients: " + connectedClients.get());
    }

    // Maneja la conexión de un cliente en un hilo del pool
    private void handleClientConnection(Socket clientSocket) {
        connectedClients.incrementAndGet();
        System.out.println("Client connected: " + clientSocket.getInetAddress() + " (Total: " + connectedClients.get() + ")");

        try {
            // Delegar al router la lectura/escritura del request/response
            router.handleRequest(clientSocket.getInputStream(), clientSocket.getOutputStream());

        } catch (IOException e) {
            // Error de I/O, el cliente probablemente cerró la conexión
            if (!clientSocket.isClosed()) {
                System.err.println("I/O error in client connection: " + clientSocket.getInetAddress() + " - " + e.getMessage());
            }
        } catch (Exception e) {
            // Cualquier otro error no controlado
            System.err.println("Unexpected error in client thread " + clientSocket.getInetAddress() + ": " + e.getMessage());
            e.printStackTrace(System.err);
            // Intentar enviar un error 500 si el socket sigue abierto (aunque el router ya lo hace)
            try {
                ResponseEntity errorResponse = createErrorResponse(500, "Internal Server Error", "An unexpected server error occurred: " + e.getMessage());
                errorResponse.writeTo(clientSocket.getOutputStream());
            } catch (Exception ignored) {}
        } finally {
            try {
                if (!clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
            connectedClients.decrementAndGet();
            System.out.println("Client disconnected: " + clientSocket.getInetAddress() + " (Total: " + connectedClients.get() + ")");
        }
    }

    // Metodo auxiliar para crear respuestas de error
    private ResponseEntity createErrorResponse(int status, String reason, String message) {
        String errorBody = String.format("{\"error\": \"%s\", \"status\": %d}", message, status);
        java.util.Map<String, String> headers = new java.util.HashMap<>();
        headers.put("Content-Type", "application/json");
        byte[] bodyBytes = errorBody.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        headers.put("Content-Length", String.valueOf(bodyBytes.length));
        return new ResponseEntity(status, reason, headers, bodyBytes);
    }

    /**
     * Shutdown the server and close the server socket if open.
     */
    public void shutdown() {
        try {
            // Stop accepting new connections
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing ServerSocket: " + e.getMessage());
        } finally {
            // Shut down executors gracefully
            clientExecutor.shutdown();
            scheduler.shutdown();
            try {
                if (!clientExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    clientExecutor.shutdownNow();
                }
                if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                clientExecutor.shutdownNow();
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            System.out.println("Server shut down.");
        }
    }
}