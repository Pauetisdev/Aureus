package cat.uvic.teknos.dam.aureus;

import cat.uvic.teknos.serialization.UserSerializer;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/>
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Server {
    public static void main(String[] args) throws IOException {
        var server = new ServerSocket(5000);
        var serializer = new UserSerializer();

        var client = server.accept();

        var inputStream = new Scanner(new InputStreamReader(client.getInputStream()));
        var outputStream = new PrintWriter(client.getOutputStream());

        outputStream.println("Hello from echo server");
        outputStream.flush();

        var request = "";
        while (!(request = inputStream.nextLine().toLowerCase()).equals("exit")) {
            // Dummy logic
            var user = new User();
            user.setId(Integer.parseInt(request));
            user.setName("User1");
            user.setEmail("user1@uvic.cat");

            outputStream.println(serializer.serialize(user));
            outputStream.flush();
        }

        client.close();
        server.close();
    }
}