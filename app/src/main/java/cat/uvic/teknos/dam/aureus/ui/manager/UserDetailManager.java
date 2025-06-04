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
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;

public class UserDetailManager {
    private final ModelFactory modelFactory;
    private final RepositoryFactory repositoryFactory;
    private final Scanner scanner;

    public UserDetailManager(ModelFactory modelFactory, RepositoryFactory repositoryFactory, Scanner scanner) {
        this.modelFactory = modelFactory;
        this.repositoryFactory = repositoryFactory;
        this.scanner = scanner;
    }

    private void displayUserDetail(Integer id) {
        var detail = repositoryFactory.getUserDetailRepository().get(id);
        if (detail != null) {
            System.out.println("\nDetails found:");
            System.out.println("----------------------------------------");
            System.out.println("ID: " + detail.getId());
            System.out.println("User: " + (detail.getUser() != null ? detail.getUser().getUsername() : "N/A"));
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
                new Column().header("User").with(d -> d.getUser() != null ? d.getUser().getUsername() : "N/A"),
                new Column().header("Phone").with(d -> d.getPhone() != null ? d.getPhone() : "N/A")
        )));
        System.out.println("----------------------------------------");
    }

    private void displayAvailableUsers() {
        Set<User> users = repositoryFactory.getUserRepository().getAll();
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

    private void createOrUpdateUserDetails() {
        try {
            // Mostrar usuarios disponibles
            displayAvailableUsers();

            System.out.println("\nEnter User ID:");
            var userId = Integer.parseInt(scanner.nextLine());
            var user = repositoryFactory.getUserRepository().get(userId);

            if (user == null) {
                System.out.println("User not found");
                return;
            }

            // Verificar si ya existen detalles
            var existingDetails = repositoryFactory.getUserDetailRepository().get(userId);
            if (existingDetails != null) {
                System.out.println("\nUser details already exist for this user:");
                displayUserDetail(userId);
                System.out.println("\nDo you want to update these details? (yes/no):");
                if (!scanner.nextLine().equalsIgnoreCase("yes")) {
                    System.out.println("Operation cancelled");
                    return;
                }
            }

            // Crear o actualizar detalles
            var detail = existingDetails != null ? existingDetails : modelFactory.newUserDetail();
            detail.setUser(user);
            detail.setId(userId);

            System.out.println("\nEnter new details (press Enter to skip/keep current value):");

            System.out.println("Phone" + (existingDetails != null && existingDetails.getPhone() != null ?
                    " [current: " + existingDetails.getPhone() + "]" : "") + ":");
            var phone = scanner.nextLine();
            detail.setPhone(phone.isEmpty() ? (existingDetails != null ? existingDetails.getPhone() : null) : phone);

            System.out.println("Gender" + (existingDetails != null && existingDetails.getGender() != null ?
                    " [current: " + existingDetails.getGender() + "]" : "") + ":");
            var gender = scanner.nextLine();
            detail.setGender(gender.isEmpty() ? (existingDetails != null ? existingDetails.getGender() : null) : gender);

            System.out.println("Nationality" + (existingDetails != null && existingDetails.getNationality() != null ?
                    " [current: " + existingDetails.getNationality() + "]" : "") + ":");
            var nationality = scanner.nextLine();
            detail.setNationality(nationality.isEmpty() ? (existingDetails != null ? existingDetails.getNationality() : null) : nationality);

            System.out.println("Birth date (YYYY-MM-DD)" + (existingDetails != null && existingDetails.getBirthdate() != null ?
                    " [current: " + existingDetails.getBirthdate().format(DateTimeFormatter.ISO_LOCAL_DATE) + "]" : "") + ":");
            var birthdateStr = scanner.nextLine();
            if (!birthdateStr.isEmpty()) {
                try {
                    var birthdate = LocalDate.parse(birthdateStr, DateTimeFormatter.ISO_LOCAL_DATE);
                    detail.setBirthdate(birthdate);
                } catch (DateTimeParseException e) {
                    System.out.println("Invalid date format. Using previous value or null.");
                    detail.setBirthdate(existingDetails != null ? existingDetails.getBirthdate() : null);
                }
            } else {
                detail.setBirthdate(existingDetails != null ? existingDetails.getBirthdate() : null);
            }

            repositoryFactory.getUserDetailRepository().save(detail);
            System.out.println("\nDetails successfully " + (existingDetails != null ? "updated" : "saved"));
            displayUserDetail(userId);

        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format");
        }
    }

    public void run() {
        while (true) {
            System.out.println("\nUser Details Management");
            System.out.println("1 - View specific user details");
            System.out.println("2 - Create/Update user details");
            System.out.println("3 - Delete user details");
            System.out.println("4 - Exit");
            System.out.print("\nSelect an option: ");

            var command = scanner.nextLine();

            switch (command) {
                case "1":
                    displayAvailableUserDetails();
                    System.out.println("\nEnter user details ID:");
                    try {
                        var id = Integer.parseInt(scanner.nextLine());
                        displayUserDetail(id);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID");
                    }
                    break;

                case "2":
                    createOrUpdateUserDetails();
                    break;

                case "3":
                    displayAvailableUserDetails();
                    System.out.println("\nEnter details ID to delete:");
                    try {
                        var id = Integer.parseInt(scanner.nextLine());
                        var detailToDelete = repositoryFactory.getUserDetailRepository().get(id);
                        if (detailToDelete != null) {
                            System.out.println("\nDetails to be deleted:");
                            displayUserDetail(detailToDelete.getId());

                            System.out.println("Are you sure you want to delete these details? (yes/no):");
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