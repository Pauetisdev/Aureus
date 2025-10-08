package cat.uvic.teknos.dam.aureus;

import cat.uvic.teknos.serialization.UserSerializer;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


public class Client {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 5000);
        var serializer = new UserSerializer();

        var inputStream = new Scanner(new InputStreamReader(socket.getInputStream()));
        var outputStream = new PrintWriter(socket.getOutputStream());

        System.out.println(inputStream.nextLine());

        var scanner = new Scanner(System.in);
        var request = "";
        while (! (request = scanner.nextLine()).equals("exit")) {
            outputStream.println(request);
            outputStream.flush();

            var userText = inputStream.nextLine();
            var user = serializer.deserialize(userText);

            System.out.println(user);
        }

        socket.close();
    }

}