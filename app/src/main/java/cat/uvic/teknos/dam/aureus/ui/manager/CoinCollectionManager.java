package cat.uvic.teknos.dam.aureus.ui.manager;

import cat.uvic.teknos.dam.aureus.ModelFactory;
import cat.uvic.teknos.dam.aureus.repositories.RepositoryFactory;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;

import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class CoinCollectionManager {
    private final ModelFactory modelFactory;
    private final RepositoryFactory repositoryFactory;
    private final Scanner scanner;

    public CoinCollectionManager(ModelFactory modelFactory, RepositoryFactory repositoryFactory, Scanner scanner) {
        this.modelFactory = modelFactory;
        this.repositoryFactory = repositoryFactory;
        this.scanner = scanner;
    }

    public void run() {
        System.out.println("Coin Collection Management, type:");
        System.out.println("1 - to show all coin collections");
        System.out.println("2 - to view collection details");
        System.out.println("3 - to add coin to collection");
        System.out.println("4 - to remove coin from collection");
        System.out.println("5 - to exit");

        var repository = repositoryFactory.getCoinCollectionRepository();

        var command = "";
        while (!Objects.equals(command = scanner.nextLine(), "5")) {
            switch (command) {
                case "1":
                    var coinCollections = repository.getAll();
                    System.out.println(AsciiTable.getTable(coinCollections, Arrays.asList(
                            new Column().header("Coin ID").with(cc -> cc.getCoinId().toString()),
                            new Column().header("Collection ID").with(cc -> cc.getCollectionId().toString()),
                            new Column().header("Coin").with(cc -> cc.getCoin() != null ?
                                    cc.getCoin().getCoinName() : "N/A"),
                            new Column().header("Collection").with(cc -> cc.getCollection() != null ?
                                    cc.getCollection().toString() : "N/A")
                    )));
                    break;

                case "2":
                    System.out.println("Enter coin ID:");
                    try {
                        var coinId = Integer.parseInt(scanner.nextLine());
                        System.out.println("Enter collection ID:");
                        var collectionId = Integer.parseInt(scanner.nextLine());

                        var matchingCollection = repository.getAll().stream()
                                .filter(cc -> Objects.equals(cc.getCoinId(), coinId) &&
                                        Objects.equals(cc.getCollectionId(), collectionId))
                                .findFirst();

                        if (matchingCollection.isPresent()) {
                            var cc = matchingCollection.get();
                            System.out.println("Details found:");
                            System.out.println("Coin ID: " + cc.getCoinId());
                            System.out.println("Collection ID: " + cc.getCollectionId());
                            if (cc.getCoin() != null) {
                                System.out.println("Coin: " + cc.getCoin().getCoinName());
                            }
                            if (cc.getCollection() != null) {
                                System.out.println("Collection: " + cc.getCollection().toString());
                            }
                        } else {
                            System.out.println("Not found");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID");
                    }
                    break;

                case "3":
                    try {
                        var coinCollection = modelFactory.newCoinCollection();

                        System.out.println("Enter coin ID:");
                        var coinId = Integer.parseInt(scanner.nextLine());
                        coinCollection.setCoinId(coinId);

                        System.out.println("Enter collection ID:");
                        var collectionId = Integer.parseInt(scanner.nextLine());
                        coinCollection.setCollectionId(collectionId);

                        var coin = repositoryFactory.getCoinRepository().get(coinId);
                        var collection = repositoryFactory.getCollectionRepository().get(collectionId);

                        if (coin != null && collection != null) {
                            coinCollection.setCoin(coin);
                            coinCollection.setCollection(collection);
                            repository.save(coinCollection);
                            System.out.println("Coin successfully added to collection");
                        } else {
                            System.out.println("Coin or collection not found");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID format");
                    }
                    break;

                case "4":
                    System.out.println("Enter coin ID:");
                    try {
                        var coinId = Integer.parseInt(scanner.nextLine());
                        System.out.println("Enter collection ID:");
                        var collectionId = Integer.parseInt(scanner.nextLine());

                        var matchingCollection = repository.getAll().stream()
                                .filter(cc -> Objects.equals(cc.getCoinId(), coinId) &&
                                        Objects.equals(cc.getCollectionId(), collectionId))
                                .findFirst();

                        if (matchingCollection.isPresent()) {
                            repository.delete(matchingCollection.get());
                            System.out.println("Successfully removed coin from collection");
                        } else {
                            System.out.println("Not found");
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