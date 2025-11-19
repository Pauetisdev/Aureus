package cat.uvic.teknos.dam.aureus;

/**
 * Entry point for the console client application.
 *
 * Responsibilities:
 * - Read host and port from environment variables or command-line arguments.
 * - Instantiate the client and start its main loop.
 *
 * Command-line args:
 * - args[0]: host (overrides AUREUS_HOST)
 * - args[1]: port (overrides AUREUS_PORT)
 */

public class ConsoleApp {
    public static void main(String[] args) {
        String host = System.getenv().getOrDefault("AUREUS_HOST", "localhost");
        int port = 5000;
        String portEnv = System.getenv("AUREUS_PORT");
        if (portEnv != null) {
            try { port = Integer.parseInt(portEnv); } catch (NumberFormatException ignored) {}
        }
        if (args.length >= 1) host = args[0];
        if (args.length >= 2) {
            try { port = Integer.parseInt(args[1]); } catch (NumberFormatException ignored) {}
        }

        Client client = new Client(host, port);
        client.run();
    }
}
