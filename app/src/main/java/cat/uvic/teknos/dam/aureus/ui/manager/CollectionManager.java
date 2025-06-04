package cat.uvic.teknos.dam.aureus.ui.manager;

import cat.uvic.teknos.dam.aureus.ModelFactory;
import cat.uvic.teknos.dam.aureus.repositories.RepositoryFactory;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;

import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class CollectionManager {
    private final ModelFactory modelFactory;
    private final RepositoryFactory repositoryFactory;
    private final Scanner scanner;

    public CollectionManager(ModelFactory modelFactory, RepositoryFactory repositoryFactory, Scanner scanner) {
        this.modelFactory = modelFactory;
        this.repositoryFactory = repositoryFactory;
        this.scanner = scanner;
    }

    public void run() {
        System.out.println("Collection Management, type:");
        System.out.println("1 - to show all collections");
        System.out.println("2 - to view specific collection");
        System.out.println("3 - to create new collection");
        System.out.println("4 - to delete a collection");
        System.out.println("5 - to exit");

        var repository = repositoryFactory.getCollectionRepository();

        var command = "";
        while (!Objects.equals(command = scanner.nextLine(), "5")) {
            switch (command) {
                case "1":
                    var collections = repository.getAll();
                    System.out.println(AsciiTable.getTable(collections, Arrays.asList(
                            new Column().header("ID").with(c -> Integer.toString(c.getId())),
                            new Column().header("Name").with(c -> c.getCollectionName()),
                            new Column().header("Description").with(c -> c.getDescription())
                    )));
                    break;

                case "2":
                    System.out.println("Enter collection ID:");
                    try {
                        var id = Integer.parseInt(scanner.nextLine());
                        var collection = repository.get(id);
                        if (collection != null) {
                            System.out.println("Collection found:");
                            System.out.println("ID: " + collection.getId());
                            System.out.println("Name: " + collection.getCollectionName());
                            System.out.println("Description: " + collection.getDescription());
                        } else {
                            System.out.println("Collection not found");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID");
                    }
                    break;

                case "3":
                    System.out.println("Enter collection name:");
                    var name = scanner.nextLine();
                    System.out.println("Enter description:");
                    var description = scanner.nextLine();

                    var newCollection = modelFactory.newCollection();
                    newCollection.setCollectionName(name);
                    newCollection.setDescription(description);

                    repository.save(newCollection);
                    System.out.println("Collection successfully saved");
                    break;

                case "4":
                    System.out.println("Enter collection ID to delete:");
                    try {
                        var id = Integer.parseInt(scanner.nextLine());
                        var collectionToDelete = repository.get(id);
                        if (collectionToDelete != null) {
                            repository.delete(collectionToDelete);
                            System.out.println("Collection successfully deleted");
                        } else {
                            System.out.println("Collection not found");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID");
                    }
                    break;

                default:
                    System.out.println("Invalid command");
                    break;
            }
        }
    }
}