package cat.uvic.teknos.dam.aureus.core;

import cat.uvic.teknos.dam.aureus.http.RequestRouter; // Importa el placeholder/clase real
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private final int port;
    private final RequestRouter router;
    private ServerSocket serverSocket;

    public Server(int port, RequestRouter router) {
        this.port = port;
        this.router = router;
    }

    public void start() {
        try {
            // Puerto 8080 por defecto
            serverSocket = new ServerSocket(port);
            System.out.println("Server started and listening on port: " + port);

            while (!serverSocket.isClosed()) {
                // 1. ACEPTAR: Bloquea hasta que un nuevo cliente se conecta.
                Socket clientSocket = serverSocket.accept();

                System.out.println("🔗 Client connected: " + clientSocket.getInetAddress());

                // 2. PROCESAR: El thread principal maneja la solicitud de forma síncrona.
                handleRequest(clientSocket);
            }
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

    private void handleRequest(Socket clientSocket) {
        try {
            // Llama al RequestRouter, pasando los streams de entrada/salida.
            // Si router.handleRequest lanza IOException, es capturada por el catch.
            router.handleRequest(clientSocket.getInputStream(), clientSocket.getOutputStream());

        } catch (IOException e) {
            System.err.println("I/O error in client connection: " + e.getMessage());
        } finally {
            try {
                // Cerramos el Socket. **IMPORTANTE** para liberar el servidor secuencial
                // y permitir que serverSocket.accept() pueda aceptar al siguiente cliente.
                clientSocket.close();
                System.out.println("Client disconnected.");
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    public void shutdown() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing ServerSocket: " + e.getMessage());
        }
        System.out.println("Server shut down.");
    }
}