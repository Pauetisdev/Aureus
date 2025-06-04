package cat.uvic.teknos.dam.aureus.ui.manager;

import cat.uvic.teknos.dam.aureus.ModelFactory;
import cat.uvic.teknos.dam.aureus.repositories.RepositoryFactory;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;

import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class CoinManager {
    private final ModelFactory modelFactory;
    private final RepositoryFactory repositoryFactory;
    private final Scanner scanner;

    public CoinManager(ModelFactory modelFactory, RepositoryFactory repositoryFactory, Scanner scanner) {
        this.modelFactory = modelFactory;
        this.repositoryFactory = repositoryFactory;
        this.scanner = scanner;
    }

    public void run() {
        System.out.println("Coin Management, type:");
        System.out.println("1 - to show all coins");
        System.out.println("2 - to search coins by material");
        System.out.println("3 - to search coins by year");
        System.out.println("4 - to add new coin");
        System.out.println("5 - to delete a coin");
        System.out.println("6 - to exit");

        var repository = repositoryFactory.getCoinRepository();

        var command = "";
        while (!Objects.equals(command = scanner.nextLine(), "6")) {
            switch (command) {
                case "1":
                    var coins = repository.getAll();
                    System.out.println(AsciiTable.getTable(coins, Arrays.asList(
                            new Column().header("ID").with(c -> Integer.toString(c.getId())),
                            new Column().header("Material").with(c -> c.getCoinMaterial()),
                            new Column().header("Year").with(c -> c.getCoinYear().toString())
                    )));
                    break;

                case "2":
                    System.out.println("Enter material:");
                    var material = scanner.nextLine();
                    var coinsByMaterial = repository.findByMaterial(material);
                    System.out.println(AsciiTable.getTable(coinsByMaterial, Arrays.asList(
                            new Column().header("ID").with(c -> Integer.toString(c.getId())),
                            new Column().header("Material").with(c -> c.getCoinMaterial()),
                            new Column().header("Year").with(c -> c.getCoinYear().toString())
                    )));
                    break;

                case "3":
                    System.out.println("Enter year:");
                    try {
                        var year = Integer.parseInt(scanner.nextLine());
                        var coinsByYear = repository.findByYear(year);
                        System.out.println(AsciiTable.getTable(coinsByYear, Arrays.asList(
                                new Column().header("ID").with(c -> Integer.toString(c.getId())),
                                new Column().header("Material").with(c -> c.getCoinMaterial()),
                                new Column().header("Year").with(c -> c.getCoinYear().toString())
                        )));
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid year");
                    }
                    break;

                case "4":
                    System.out.println("Enter material:");
                    var newMaterial = scanner.nextLine();
                    System.out.println("Enter year:");
                    try {
                        var newYear = Integer.parseInt(scanner.nextLine());
                        var coin = modelFactory.newCoin();
                        coin.setCoinMaterial(newMaterial);
                        coin.setCoinYear(newYear);
                        repository.save(coin);
                        System.out.println("Coin successfully saved");
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid year");
                    }
                    break;

                case "5":
                    System.out.println("Enter coin ID to delete:");
                    try {
                        var id = Integer.parseInt(scanner.nextLine());
                        var coinToDelete = repository.get(id);
                        if (coinToDelete != null) {
                            repository.delete(coinToDelete);
                            System.out.println("Coin successfully deleted");
                        } else {
                            System.out.println("Coin not found");
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