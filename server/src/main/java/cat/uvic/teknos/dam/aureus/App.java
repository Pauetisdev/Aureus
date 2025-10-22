package cat.uvic.teknos.dam.aureus;

import cat.uvic.teknos.dam.aureus.configuration.DependencyInjector;
import cat.uvic.teknos.dam.aureus.core.Server;

public class App {
    public static void main(String[] args) {
        int port = 5000;
        // 1) If a command-line arg is provided, use it
        if (args != null && args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port argument '" + args[0] + "', falling back to default 5000");
            }
        } else {
            // 2) If environment variable AUREUS_PORT is set, use it
            String envPort = System.getenv("AUREUS_PORT");
            if (envPort != null && !envPort.isBlank()) {
                try {
                    port = Integer.parseInt(envPort);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid AUREUS_PORT value '" + envPort + "', falling back to default 5000");
                }
            }
        }

        Server server = DependencyInjector.provideServer(port);
        server.start();
    }
}
