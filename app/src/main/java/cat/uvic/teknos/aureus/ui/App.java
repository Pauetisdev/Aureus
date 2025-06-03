package cat.uvic.teknos.aureus.ui;

import cat.uvic.teknos.dam.aureus.ModelFactory;
import cat.uvic.teknos.dam.aureus.repositories.RepositoryFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Scanner;

public class App {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        var scanner = new Scanner(System.in);

        Banner.show();

        var diManager = new DIManager();
        RepositoryFactory repositoryFactory = diManager.get("repository_factory");
        ModelFactory modelFactory = diManager.get("model_factory");

        showMainMenu();

        var command = "";
        while  (!Objects.equals(command = scanner.nextLine(), "exit")) {

            switch (command) {
                case "1":
                    manageUser(repositoryFactory, modelFactory, scanner);
                    showMainMenu();
                    break;
            }
        }

    }

    private static void showMainMenu() {
        System.out.println("Welcome to the BandHub back-office! Type:");
        System.out.println("1 - to manage the instruments");
    }

    private static void manageUser(RepositoryFactory repositoryFactory, ModelFactory modelFactory, Scanner scanner) {
        new UserManager(modelFactory, repositoryFactory, scanner).run();
    }
}