package cat.uvic.teknos.dam.aureus.ui.manager;

import cat.uvic.teknos.dam.aureus.ModelFactory;
import cat.uvic.teknos.dam.aureus.User;
import cat.uvic.teknos.dam.aureus.UserDetail;
import cat.uvic.teknos.dam.aureus.repositories.RepositoryFactory;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.List;

/**
 * Manages user details operations including creation, display, and deletion.
 * This class provides a command-line interface for managing user details in the system.
 *
 * @author Pau Vilardell
 * @version 1.0
 * @since 2025-06-04
 */
public class UserDetailManager {
    private final ModelFactory modelFactory;
    private final RepositoryFactory repositoryFactory;
    private final Scanner scanner;

    /**
     * Constructs a new UserDetailManager with the required dependencies.
     *
     * @param modelFactory The factory for creating model objects
     * @param repositoryFactory The factory for accessing data repositories
     * @param scanner Scanner for reading user input
     */
    public UserDetailManager(ModelFactory modelFactory, RepositoryFactory repositoryFactory, Scanner scanner) {
        this.modelFactory = modelFactory;
        this.repositoryFactory = repositoryFactory;
        this.scanner = scanner;
    }

    /**
     * Displays the details of a specific user.
     *
     * @param id The ID of the user whose details should be displayed
     */
    private void displayUserDetail(Integer id) {
        var detail = repositoryFactory.getUserDetailRepository().get(id);
        if (detail != null) {
            User user = repositoryFactory.getUserRepository().get(detail.getId());
            String username = (user != null) ? user.getUsername() : "-";

            System.out.println("\nDetails found:");
            System.out.println("----------------------------------------");
            System.out.println("ID: " + detail.getId());
            System.out.println("User: " + username);
            System.out.println("Phone: " + (detail.getPhone() != null ? detail.getPhone() : "N/A"));
            System.out.println("Gender: " + (detail.getGender() != null ? detail.getGender() : "N/A"));
            System.out.println("Nationality: " + (detail.getNationality() != null ? detail.getNationality() : "N/A"));
            System.out.println("Birth Date: " + (detail.getBirthdate() != null ?
                    detail.getBirthdate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "N/A"));
            System.out.println("----------------------------------------");
        } else {
            System.out.println("Details not found for ID: " + id);
        }
    }


    /**
     * Displays a list of all available user details in the system.
     * Uses ASCII table format for better readability.
     */
    private void displayAvailableUserDetails() {
        var details = repositoryFactory.getUserDetailRepository().getAll();
        if (details.isEmpty()) {
            System.out.println("No user details available");
            return;
        }

        System.out.println("\nAvailable user details:");
        System.out.println("----------------------------------------");
        System.out.println(AsciiTable.getTable(details, Arrays.asList(
                new Column().header("ID").with(d -> Integer.toString(d.getId())),
                new Column().header("User").with(d -> {
                    User user = repositoryFactory.getUserRepository().get(d.getId());
                    return (user != null) ? user.getUsername() : "-";
                }),
                new Column().header("Phone").with(d -> d.getPhone() != null ? d.getPhone() : "N/A")
        )));
        System.out.println("----------------------------------------");
    }


    /**
     * Displays a list of all available users in the system.
     * Uses ASCII table format for better readability.
     */
    private void displayAvailableUsers() {
        List<User> users = repositoryFactory.getUserRepository().getAll();
        if (users.isEmpty()) {
            System.out.println("No users available in the system");
            return;
        }

        System.out.println("\nAvailable users:");
        System.out.println("----------------------------------------");
        System.out.println(AsciiTable.getTable(users, Arrays.asList(
                new Column().header("ID").with(u -> Integer.toString(u.getId())),
                new Column().header("Username").with(User::getUsername),
                new Column().header("Email").with(User::getEmail)
        )));
        System.out.println("----------------------------------------");
    }

    /**
     * Creates new user details for a user who doesn't have any existing details.
     * If the user already has details, the operation will be cancelled.
     */
    private void createNewUserDetails() {
        try {
            displayAvailableUsers();

            System.out.print("\nEnter User ID:");
            var userId = Integer.parseInt(scanner.nextLine());
            var user = repositoryFactory.getUserRepository().get(userId);

            if (user == null) {
                System.out.println("User not found");
                return;
            }

            var existingDetails = repositoryFactory.getUserDetailRepository().get(userId);
            if (existingDetails != null) {
                System.out.println("\nUser details already exist for this user. Cannot create new details.");
                displayUserDetail(userId);
                return;
            }

            var detail = modelFactory.newUserDetail();
            detail.setUser(user);
            detail.setId(userId);

            System.out.println("\nEnter new details:");

            System.out.print("Phone: ");
            var phone = scanner.nextLine();
            detail.setPhone(phone.isEmpty() ? null : phone);

            System.out.print("Gender: ");
            var gender = scanner.nextLine();
            detail.setGender(gender.isEmpty() ? null : gender);

            System.out.print("Nationality: ");
            var nationality = scanner.nextLine();
            detail.setNationality(nationality.isEmpty() ? null : nationality);

            System.out.print("Birth date (YYYY-MM-DD):");
            var birthdateStr = scanner.nextLine();
            if (!birthdateStr.isEmpty()) {
                try {
                    var birthdate = LocalDate.parse(birthdateStr, DateTimeFormatter.ISO_LOCAL_DATE);
                    detail.setBirthdate(birthdate);
                } catch (DateTimeParseException e) {
                    System.out.println("Invalid date format. Setting birthdate to null.");
                    detail.setBirthdate(null);
                }
            }

            repositoryFactory.getUserDetailRepository().save(detail);
            System.out.println("\nDetails successfully saved");
            displayUserDetail(userId);

        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format");
        }
    }

    /**
     * Main method that runs the user detail management interface.
     * Provides a menu with options to:
     * 1. View specific user details
     * 2. Create new user details
     * 3. Delete user details
     * 4. Exit
     */
    public void run() {
        while (true) {
            System.out.println("\nUser Details Management");
            System.out.println("1 - View specific user details");
            System.out.println("2 - Create new user details");
            System.out.println("3 - Delete user details");
            System.out.println("4 - Exit");
            System.out.print("\nSelect an option: ");

            var command = scanner.nextLine();

            switch (command) {
                case "1":
                    displayAvailableUserDetails();
                    System.out.print("\nEnter user details ID:");
                    try {
                        var id = Integer.parseInt(scanner.nextLine());
                        displayUserDetail(id);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID");
                    }
                    break;

                case "2":
                    createNewUserDetails();
                    break;

                case "3":
                    displayAvailableUserDetails();
                    System.out.print("\nEnter details ID to delete:");
                    try {
                        var id = Integer.parseInt(scanner.nextLine());
                        var detailToDelete = repositoryFactory.getUserDetailRepository().get(id);
                        if (detailToDelete != null) {
                            displayUserDetail(detailToDelete.getId());

                            System.out.print("Are you sure you want to delete these details? (yes/no):");
                            if (scanner.nextLine().equalsIgnoreCase("yes")) {
                                repositoryFactory.getUserDetailRepository().delete(detailToDelete);
                                System.out.println("Details successfully deleted");
                            } else {
                                System.out.println("Delete operation cancelled");
                            }
                        } else {
                            System.out.println("Details not found");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID");
                    }
                    break;

                case "4":
                    return;

                default:
                    System.out.println("Invalid command");
                    break;
            }
        }
    }
}