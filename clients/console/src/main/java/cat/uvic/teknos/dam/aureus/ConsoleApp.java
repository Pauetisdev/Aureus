package cat.uvic.teknos.dam.aureus;

public class ConsoleApp {
    public static void main(String[] args) {
        String host = System.getenv().getOrDefault("AUREUS_HOST", "localhost");
        int port = 8080;
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
