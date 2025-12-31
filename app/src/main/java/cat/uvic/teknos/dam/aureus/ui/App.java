
package cat.uvic.teknos.dam.aureus.ui;

import cat.uvic.teknos.dam.aureus.ModelFactory;
import cat.uvic.teknos.dam.aureus.repositories.RepositoryFactory;
import cat.uvic.teknos.dam.aureus.ui.manager.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Scanner;

/**
 * Main application class for the AUREUS coin collection and trading system.
 * This class serves as the entry point and provides the main menu interface
 * to access different management functionalities of the system.
 *
 * @author Pau Vilardell
 * @version 1.0
 * @since 2025-06-05
 */
public class App {
    /**
     * Main entry point of the application.
     * Initializes the system components and runs the main menu loop.
     *
     * @param args Command line arguments (not used)
     * @throws IOException If there's an error loading configuration
     * @throws ClassNotFoundException If a required class cannot be found
     * @throws InvocationTargetException If there's an error instantiating components
     * @throws InstantiationException If there's an error creating objects
     * @throws IllegalAccessException If there's an error accessing components
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        // Inicializamos el scanner para leer entrada del usuario
        var scanner = new Scanner(System.in);
        scanner.useDelimiter(System.lineSeparator());

        // Mostramos el banner de la aplicación
        Banner.show();

        // Inicializamos el gestor de dependencias y obtenemos las factorías necesarias
        var diManager = new DIManager();
        RepositoryFactory repositoryFactory = diManager.get("repository_factory");
        ModelFactory modelFactory = diManager.get("model_factory");

        // Mostramos el menú principal
        showMainMenu();

        // Bucle principal del programa
        var command = "";
        while (true) {
            System.out.print(""); // Asegura que el prompt se muestre

            if (scanner.hasNextLine()) {
                command = scanner.nextLine().trim();

                if (Objects.equals(command, "exit")) {
                    break;
                }

                switch (command) {
                    case "1":
                        manageUsers(repositoryFactory, modelFactory, scanner);
                        showMainMenu();
                        break;
                    case "2":
                        manageUserDetails(repositoryFactory, modelFactory, scanner);
                        showMainMenu();
                        break;
                    case "3":
                        manageCoins(repositoryFactory, modelFactory, scanner);
                        showMainMenu();
                        break;
                    case "4":
                        manageCollections(repositoryFactory, modelFactory, scanner);
                        showMainMenu();
                        break;
                    case "5":
                        manageTransactions(repositoryFactory, modelFactory, scanner);
                        showMainMenu();
                        break;
                    case "6":
                        manageCoinTransactions(repositoryFactory, modelFactory, scanner);
                        showMainMenu();
                        break;
                    default:
                        System.out.println("Invalid command. Please try again.");
                        showMainMenu();
                        break;
                }
            } else {
                // Si no hay entrada disponible, espera un poco
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    /**
     * Displays the main menu options to the user.
     * Shows all available management functionalities in the system.
     */
    private static void showMainMenu() {
        // Mostramos el menú principal con todas las opciones disponibles
        System.out.println("\n=== AUREUS - Management System ===");
        System.out.println("1 - Manage Users");
        System.out.println("2 - Manage User Details");
        System.out.println("3 - Manage Coins");
        System.out.println("4 - Manage Collections");
        System.out.println("5 - Manage Transactions");
        System.out.println("6 - Manage Coin Transactions");
        System.out.println("'exit' - Exit Program");
        System.out.print("\nSelect an option: ");
        System.out.flush();
    }

    /**
     * Initializes and runs the User Management interface.
     *
     * @param repositoryFactory Factory for accessing data repositories
     * @param modelFactory Factory for creating domain objects
     * @param scanner Scanner for reading user input
     */
    private static void manageUsers(RepositoryFactory repositoryFactory, ModelFactory modelFactory, Scanner scanner) {
        // Iniciamos el gestor de usuarios
        new UserManager(modelFactory, repositoryFactory, scanner).run();
    }

    /**
     * Initializes and runs the User Details Management interface.
     *
     * @param repositoryFactory Factory for accessing data repositories
     * @param modelFactory Factory for creating domain objects
     * @param scanner Scanner for reading user input
     */
    private static void manageUserDetails(RepositoryFactory repositoryFactory, ModelFactory modelFactory, Scanner scanner) {
        // Iniciamos el gestor de detalles de usuario
        new UserDetailManager(modelFactory, repositoryFactory, scanner).run();
    }

    /**
     * Initializes and runs the Coin Management interface.
     *
     * @param repositoryFactory Factory for accessing data repositories
     * @param modelFactory Factory for creating domain objects
     * @param scanner Scanner for reading user input
     */
    private static void manageCoins(RepositoryFactory repositoryFactory, ModelFactory modelFactory, Scanner scanner) {
        // Iniciamos el gestor de monedas
        new CoinManager(modelFactory, repositoryFactory, scanner).run();
    }

    /**
     * Initializes and runs the Collection Management interface.
     *
     * @param repositoryFactory Factory for accessing data repositories
     * @param modelFactory Factory for creating domain objects
     * @param scanner Scanner for reading user input
     */
    private static void manageCollections(RepositoryFactory repositoryFactory, ModelFactory modelFactory, Scanner scanner) {
        // Iniciamos el gestor de colecciones
        new CollectionManager(modelFactory, repositoryFactory, scanner).run();
    }

    /**
     * Initializes and runs the Transaction Management interface.
     *
     * @param repositoryFactory Factory for accessing data repositories
     * @param modelFactory Factory for creating domain objects
     * @param scanner Scanner for reading user input
     */
    private static void manageTransactions(RepositoryFactory repositoryFactory, ModelFactory modelFactory, Scanner scanner) {
        // Iniciamos el gestor de transacciones
        new TransactionManager(modelFactory, repositoryFactory, scanner).run();
    }

    /**
     * Initializes and runs the Coin Transaction Management interface.
     *
     * @param repositoryFactory Factory for accessing data repositories
     * @param modelFactory Factory for creating domain objects
     * @param scanner Scanner for reading user input
     */
    private static void manageCoinTransactions(RepositoryFactory repositoryFactory, ModelFactory modelFactory, Scanner scanner) {
        // Iniciamos el gestor de transacciones de monedas
        new CoinTransactionManager(modelFactory, repositoryFactory, scanner).run();
    }
}