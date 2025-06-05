package cat.uvic.teknos.dam.aureus.ui.manager;

import cat.uvic.teknos.dam.aureus.ModelFactory;
import cat.uvic.teknos.dam.aureus.repositories.RepositoryFactory;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;

import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

/**
 * Manages coin collections in the AUREUS system.
 * Provides functionality for creating, viewing, updating and deleting collections,
 * as well as managing coins within collections.
 *
 * @author Pau Vilardell
 * @version 1.0
 * @since 2025-06-05
 */
public class CollectionManager {
    private final ModelFactory modelFactory;
    private final RepositoryFactory repositoryFactory;
    private final Scanner scanner;

    /**
     * Constructs a new CollectionManager with the necessary dependencies.
     *
     * @param modelFactory Factory for creating domain model objects
     * @param repositoryFactory Factory for accessing data repositories
     * @param scanner Scanner for reading user input
     */
    public CollectionManager(ModelFactory modelFactory, RepositoryFactory repositoryFactory, Scanner scanner) {
        this.modelFactory = modelFactory;
        this.repositoryFactory = repositoryFactory;
        this.scanner = scanner;
    }

    /**
     * Runs the main collection management interface.
     * Provides a menu-driven interface for all collection-related operations.
     */
    public void run() {
        while (true) {
            System.out.println("\n=== Collection Management ===");
            System.out.println("1 - View all collections");
            System.out.println("2 - View collection details");
            System.out.println("3 - Create new collection");
            System.out.println("4 - Delete collection");
            System.out.println("5 - Add coin to collection");
            System.out.println("6 - Remove coin from collection");
            System.out.println("7 - Exit");
            System.out.print("\nSelect an option: ");

            var command = scanner.nextLine();

            if (Objects.equals(command, "7")) {
                break;
            }

            try {
                switch (command) {
                    case "1" -> displayAllCollections();
                    case "2" -> viewCollectionDetails();
                    case "3" -> createNewCollection();
                    case "4" -> deleteCollection();
                    case "5" -> addCoinToCollection();
                    case "6" -> removeCoinFromCollection();
                    default -> System.out.println("Invalid command");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    /**
     * Displays all available collections in a formatted table.
     * If no collections exist, displays an appropriate message.
     */
    private void displayAllCollections() {
        var collections = repositoryFactory.getCollectionRepository().getAll();
        if (collections.isEmpty()) {
            System.out.println("No collections available");
            return;
        }
        System.out.println("\nAvailable collections:");
        System.out.println("----------------------------------------");
        System.out.println(AsciiTable.getTable(collections, Arrays.asList(
                new Column().header("ID").with(c -> Integer.toString(c.getId())),
                new Column().header("Name").with(c -> c.getCollectionName()),
                new Column().header("Description").with(c -> c.getDescription())
        )));
        System.out.println("----------------------------------------");
    }

    /**
     * Displays detailed information about a specific collection.
     * Shows both collection details and the coins it contains.
     */
    private void viewCollectionDetails() {
        displayAllCollections();
        System.out.println("\nEnter collection ID:");
        try {
            var id = Integer.parseInt(scanner.nextLine());
            displayCollectionDetails(id);
            displayCollectionCoins(id);
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID");
        }
    }

    /**
     * Displays detailed information about a collection given its ID.
     *
     * @param id The ID of the collection to display
     */
    private void displayCollectionDetails(Integer id) {
        var collection = repositoryFactory.getCollectionRepository().get(id);
        if (collection != null) {
            System.out.println("\nCollection Details:");
            System.out.println("----------------------------------------");
            System.out.println("ID: " + collection.getId());
            System.out.println("Name: " + collection.getCollectionName());
            System.out.println("Description: " + collection.getDescription());
            System.out.println("----------------------------------------");
        } else {
            System.out.println("Collection not found");
        }
    }

    /**
     * Displays all coins that belong to a specific collection.
     *
     * @param collectionId The ID of the collection whose coins should be displayed
     */
    private void displayCollectionCoins(Integer collectionId) {
        var coinCollections = repositoryFactory.getCoinCollectionRepository().findByCollectionId(collectionId);
        if (!coinCollections.isEmpty()) {
            System.out.println("\nCoins in this collection:");
            System.out.println(AsciiTable.getTable(coinCollections, Arrays.asList(
                    new Column().header("Coin ID").with(cc -> Integer.toString(cc.getCoin().getId())),
                    new Column().header("Name").with(cc -> cc.getCoin().getCoinName()),
                    new Column().header("Year").with(cc -> cc.getCoin().getCoinYear().toString()),
                    new Column().header("Material").with(cc -> cc.getCoin().getCoinMaterial())
            )));
        } else {
            System.out.println("\nNo coins in this collection");
        }
    }

    /**
     * Creates a new collection by gathering information from user input.
     * Validates the input and saves the new collection to the repository.
     */
    private void createNewCollection() {
        System.out.println("\nEnter new collection details:");

        System.out.println("Enter collection name:");
        var name = scanner.nextLine();
        if (name.trim().isEmpty()) {
            System.out.println("Collection name cannot be empty");
            return;
        }

        System.out.println("Enter description:");
        var description = scanner.nextLine();

        var newCollection = modelFactory.newCollection();
        newCollection.setCollectionName(name);
        newCollection.setDescription(description);

        repositoryFactory.getCollectionRepository().save(newCollection);
        System.out.println("\nCollection successfully created");
        displayCollectionDetails(newCollection.getId());
    }

    /**
     * Deletes an existing collection after confirmation from the user.
     * Shows collection details before deletion for verification.
     */
    private void deleteCollection() {
        displayAllCollections();
        System.out.println("\nEnter collection ID to delete:");
        try {
            var id = Integer.parseInt(scanner.nextLine());
            var collectionToDelete = repositoryFactory.getCollectionRepository().get(id);
            if (collectionToDelete != null) {
                System.out.println("\nCollection to delete:");
                displayCollectionDetails(id);
                displayCollectionCoins(id);
                System.out.println("\nAre you sure you want to delete this collection? (yes/no):");
                if (scanner.nextLine().equalsIgnoreCase("yes")) {
                    repositoryFactory.getCollectionRepository().delete(collectionToDelete);
                    System.out.println("Collection successfully deleted");
                } else {
                    System.out.println("Delete operation cancelled");
                }
            } else {
                System.out.println("Collection not found");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID");
        }
    }

    /**
     * Adds a coin to an existing collection.
     * Displays available coins and allows user to select one to add.
     */
    private void addCoinToCollection() {
        displayAllCollections();
        System.out.print("\nEnter collection ID: ");
        try {
            var collectionId = Integer.parseInt(scanner.nextLine());
            var collection = repositoryFactory.getCollectionRepository().get(collectionId);

            if (collection == null) {
                System.out.println("Collection not found");
                return;
            }

            var coins = repositoryFactory.getCoinRepository().getAll();
            if (coins.isEmpty()) {
                System.out.println("No coins available");
                return;
            }

            System.out.println("\nAvailable coins:");
            System.out.println(AsciiTable.getTable(coins, Arrays.asList(
                    new Column().header("ID").with(c -> Integer.toString(c.getId())),
                    new Column().header("Name").with(c -> c.getCoinName()),
                    new Column().header("Year").with(c -> c.getCoinYear().toString()),
                    new Column().header("Material").with(c -> c.getCoinMaterial())
            )));

            System.out.print("\nEnter coin ID to add: ");
            var coinId = Integer.parseInt(scanner.nextLine());
            var coin = repositoryFactory.getCoinRepository().get(coinId);

            if (coin != null) {
                var coinCollection = modelFactory.newCoinCollection();
                coinCollection.setCoin(coin);
                coinCollection.setCollection(collection);
                repositoryFactory.getCoinCollectionRepository().save(coinCollection);
                System.out.println("Coin successfully added to collection");
            } else {
                System.out.println("Coin not found");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format");
        }
    }

    /**
     * Removes a coin from an existing collection.
     * Shows coins in the collection and allows user to select one to remove.
     */
    private void removeCoinFromCollection() {
        displayAllCollections();
        System.out.print("\nEnter collection ID: ");
        try {
            var collectionId = Integer.parseInt(scanner.nextLine());
            var coinCollections = repositoryFactory.getCoinCollectionRepository().findByCollectionId(collectionId);

            if (coinCollections.isEmpty()) {
                System.out.println("No coins in this collection");
                return;
            }

            System.out.println("\nCoins in collection:");
            System.out.println(AsciiTable.getTable(coinCollections, Arrays.asList(
                    new Column().header("Coin ID").with(cc -> Integer.toString(cc.getCoin().getId())),
                    new Column().header("Name").with(cc -> cc.getCoin().getCoinName()),
                    new Column().header("Year").with(cc -> cc.getCoin().getCoinYear().toString()),
                    new Column().header("Material").with(cc -> cc.getCoin().getCoinMaterial())
            )));

            System.out.print("\nEnter coin ID to remove: ");
            var coinId = Integer.parseInt(scanner.nextLine());

            var coinToRemove = coinCollections.stream()
                    .filter(cc -> cc.getCoin().getId().equals(coinId))
                    .findFirst();

            if (coinToRemove.isPresent()) {
                repositoryFactory.getCoinCollectionRepository().delete(coinToRemove.get());
                System.out.println("Coin successfully removed from collection");
            } else {
                System.out.println("Coin not found in this collection");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format");
        }
    }
}