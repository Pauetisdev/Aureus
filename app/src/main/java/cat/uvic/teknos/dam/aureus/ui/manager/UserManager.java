
package cat.uvic.teknos.dam.aureus.ui.manager;

import cat.uvic.teknos.dam.aureus.ModelFactory;
import cat.uvic.teknos.dam.aureus.repositories.RepositoryFactory;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;

import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;
import java.time.LocalDateTime;

/**
 * Manages user operations through a command-line interface.
 * Provides functionality for creating, reading, and deleting users.
 * Uses ASCII tables for displaying user information in a formatted manner.
 *  @author Pau Vilardell
 *  @version 1.0
 *  @since 2025-06-04
 */
public class UserManager {
    private ModelFactory modelFactory;
    private RepositoryFactory repositoryFactory;
    private Scanner scanner;


    /**
     * Constructs a new UserManager with the required dependencies.
     *
     * @param modelFactory Factory for creating model objects
     * @param repositoryFactory Factory for accessing repositories
     * @param scanner Scanner for reading user input
     */
    public UserManager(ModelFactory modelFactory, RepositoryFactory repositoryFactory, Scanner scanner) {
        this.modelFactory = modelFactory;
        this.repositoryFactory = repositoryFactory;
        this.scanner = scanner;
    }

    /**
     * Displays all users in a formatted ASCII table.
     * Shows ID, username, email, and join date for each user.
     */
    private void displayAllUsers() {
        var users = repositoryFactory.getUserRepository().getAll();
        System.out.println(AsciiTable.getTable(users, Arrays.asList(
                new Column().header("ID").with(u -> Integer.toString(u.getId())),
                new Column().header("Username").with(u -> u.getUsername()),
                new Column().header("Email").with(u -> u.getEmail()),
                new Column().header("Join Date").with(u -> u.getJoinDate().toString())
        )));
    }

    /**
     * Runs the user management interface.
     * Provides a menu with the following options:
     * 1. Show all users
     * 2. View specific user details
     * 3. Create new user
     * 4. Delete existing user
     * 5. Exit
     *
     * Handles user input and executes corresponding operations.
     */
    public void run() {
        var repository = repositoryFactory.getUserRepository();
        var command = "";

        while (!Objects.equals(command, "5")) {
            // 👉 Muestra siempre el menú antes de pedir comando
            System.out.println("\nUser Management, type: ");
            System.out.println("1 - to show all users list");
            System.out.println("2 - to view specific user");
            System.out.println("3 - to save new user");
            System.out.println("4 - to delete existing user");
            System.out.println("5 - to exit user menu");
            System.out.print("Select an option: ");

            command = scanner.nextLine();

            switch (command) {
                case "1":
                    displayAllUsers();
                    break;

                case "2":
                    displayAllUsers();
                    System.out.println("\nEnter user ID to view details: ");
                    try {
                        var id = Integer.parseInt(scanner.nextLine());
                        var user = repository.get(id);
                        if (user != null) {
                            System.out.println("User found:");
                            System.out.println("ID: " + user.getId());
                            System.out.println("Username: " + user.getUsername());
                            System.out.println("Email: " + user.getEmail());
                            System.out.println("Join Date: " + user.getJoinDate());
                        } else {
                            System.out.println("User not found");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID");
                    }
                    break;

                case "3":
                    System.out.print("Enter username: ");
                    var username = scanner.nextLine();
                    System.out.print("Enter email: ");
                    var newEmail = scanner.nextLine();

                    if (repository.findByEmail(newEmail) != null) {
                        System.out.print("Error: A user with this email already exists");
                        break;
                    }

                    System.out.print("Enter password: ");
                    var password = scanner.nextLine();

                    var newUser = modelFactory.newUser();
                    newUser.setUsername(username);
                    newUser.setEmail(newEmail);
                    newUser.setPasswordHash(password);
                    newUser.setJoinDate(LocalDateTime.now());

                    repository.save(newUser);
                    System.out.println("User successfully saved");
                    break;

                case "4":
                    displayAllUsers();
                    System.out.println("\nEnter user ID to delete: ");
                    try {
                        var id = Integer.parseInt(scanner.nextLine());
                        var userToDelete = repository.get(id);
                        if (userToDelete != null) {
                            repository.delete(userToDelete);
                            System.out.println("User successfully deleted");
                        } else {
                            System.out.println("User not found");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID");
                    }
                    break;

                case "5":
                    System.out.println("Exiting user menu...");
                    break;

                default:
                    System.out.println("Invalid command");
                    break;
            }
        }
    }

}