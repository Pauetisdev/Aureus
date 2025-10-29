package cat.uvic.teknos.dam.aureus;

import cat.uvic.teknos.dam.aureus.configuration.DependencyInjector;
import cat.uvic.teknos.dam.aureus.core.Server;

/**
 * Server application entry point for the AUREUS project.
 *
 * <p>Initializes dependencies via {@link cat.uvic.teknos.dam.aureus.configuration.DependencyInjector}
 * and starts the embedded HTTP server.</p>
 */
public class App {
    public static void main(String[] args) {
        int port = 5000;
        Server server = DependencyInjector.provideServer(port);
        server.start();
    }
}
