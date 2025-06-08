package cat.uvic.teknos.dam.aureus.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Banner {
    public static void show() {
        try (InputStream is = Banner.class.getResourceAsStream("/banner.txt")) {
            if (is == null) {
                System.out.println("Banner no encontrado.");
                return;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error al mostrar el banner: " + e.getMessage());
        }
    }
}