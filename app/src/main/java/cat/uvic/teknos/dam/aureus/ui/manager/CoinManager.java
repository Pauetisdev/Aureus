package cat.uvic.teknos.dam.aureus.ui.manager;

import cat.uvic.teknos.dam.aureus.Coin;
import cat.uvic.teknos.dam.aureus.ModelFactory;
import cat.uvic.teknos.dam.aureus.repositories.RepositoryFactory;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

/**
 * Manager class for handling coin-related operations in the AUREUS system.
 * Provides functionality for viewing, adding, searching, and deleting coins.
 *
 * @author Pau
 */
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
        while (true) {
            System.out.println("\n=== Coin Management ===");
            System.out.println("1 - Show all coins");
            System.out.println("2 - Search coins by material");
            System.out.println("3 - Search coins by year");
            System.out.println("4 - Add new coin");
            System.out.println("5 - Delete a coin");
            System.out.println("6 - Exit");
            System.out.print("\nSelect an option: ");

            var repository = repositoryFactory.getCoinRepository();
            var command = scanner.nextLine();

            if (Objects.equals(command, "6")) {
                break;
            }

            try {
                switch (command) {
                    case "1" -> {
                        var coins = repository.getAll();
                        if (coins.isEmpty()) {
                            System.out.println("No coins found");
                        } else {
                            System.out.println(AsciiTable.getTable(coins, Arrays.asList(
                                    new Column().header("ID").with(c -> Integer.toString(((Coin) c).getId())),
                                    new Column().header("Name").with(c -> ((Coin) c).getCoinName())
                            )));
                        }
                    }

                    case "2", "3" -> {
                        var coins = switch (command) {
                            case "2" -> {
                                System.out.print("Enter material: ");
                                yield repository.findByMaterial(scanner.nextLine());
                            }
                            case "3" -> {
                                System.out.print("Enter year: ");
                                try {
                                    yield repository.findByYear(Integer.parseInt(scanner.nextLine()));
                                } catch (NumberFormatException e) {
                                    System.out.println("Invalid year format");
                                    yield null;
                                }
                            }
                            default -> null;
                        };

                        if (coins != null) {
                            if (coins.isEmpty()) {
                                System.out.println("No coins found matching the criteria");
                            } else {
                                System.out.println(AsciiTable.getTable(coins, Arrays.asList(
                                        new Column().header("ID").with(c -> Integer.toString(((Coin) c).getId())),
                                        new Column().header("Name").with(c -> ((Coin) c).getCoinName()),
                                        new Column().header("Year").with(c -> ((Coin) c).getCoinYear().toString()),
                                        new Column().header("Material").with(c -> ((Coin) c).getCoinMaterial()),
                                        new Column().header("Weight").with(c -> ((Coin) c).getCoinWeight().toString()),
                                        new Column().header("Diameter").with(c -> ((Coin) c).getCoinDiameter().toString()),
                                        new Column().header("Value").with(c -> ((Coin) c).getEstimatedValue().toString()),
                                        new Column().header("Country").with(c -> ((Coin) c).getOriginCountry()),
                                        new Column().header("Significance").with(c -> ((Coin) c).getHistoricalSignificance())
                                )));
                            }
                        }
                    }

                    case "4" -> {
                        var coin = modelFactory.newCoin();

                        System.out.print("Enter name: ");
                        coin.setCoinName(scanner.nextLine());

                        System.out.print("Enter year: ");
                        try {
                            coin.setCoinYear(Integer.parseInt(scanner.nextLine()));
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid year format");
                            continue;
                        }

                        System.out.print("Enter material: ");
                        coin.setCoinMaterial(scanner.nextLine());

                        System.out.print("Enter weight: ");
                        try {
                            coin.setCoinWeight(new BigDecimal(scanner.nextLine()));
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid weight format");
                            continue;
                        }

                        System.out.print("Enter diameter: ");
                        try {
                            coin.setCoinDiameter(new BigDecimal(scanner.nextLine()));
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid diameter format");
                            continue;
                        }

                        System.out.print("Enter estimated value: ");
                        try {
                            coin.setEstimatedValue(new BigDecimal(scanner.nextLine()));
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid value format");
                            continue;
                        }

                        System.out.print("Enter origin country: ");
                        coin.setOriginCountry(scanner.nextLine());

                        System.out.print("Enter historical significance: ");
                        coin.setHistoricalSignificance(scanner.nextLine());

                        // Mostrar resumen
                        System.out.println("\nCoin Summary:");
                        System.out.println("----------------------------------------");
                        System.out.println("Name: " + coin.getCoinName());
                        System.out.println("Year: " + coin.getCoinYear());
                        System.out.println("Material: " + coin.getCoinMaterial());
                        System.out.println("Weight: " + coin.getCoinWeight());
                        System.out.println("Diameter: " + coin.getCoinDiameter());
                        System.out.println("Estimated Value: " + coin.getEstimatedValue());
                        System.out.println("Country: " + coin.getOriginCountry());
                        System.out.println("Significance: " + coin.getHistoricalSignificance());
                        System.out.println("----------------------------------------");

                        System.out.print("\nSave this coin? (yes/no): ");
                        if (scanner.nextLine().equalsIgnoreCase("yes")) {
                            repository.save(coin);
                            System.out.println("Coin successfully saved");
                        } else {
                            System.out.println("Operation cancelled");
                        }
                    }

                    case "5" -> {
                        var coins = repository.getAll();
                        if (coins.isEmpty()) {
                            System.out.println("No coins available to delete");
                            break;
                        }


                        System.out.println("Available coins:");
                        System.out.println(AsciiTable.getTable(coins, Arrays.asList(
                                new Column().header("ID").with(c -> Integer.toString(((Coin) c).getId())),
                                new Column().header("Name").with(c -> ((Coin) c).getCoinName()),
                                new Column().header("Year").with(c -> ((Coin) c).getCoinYear().toString())
                        )));

                        System.out.print("Enter coin ID to delete: ");
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
                    }

                    default -> System.out.println("Invalid command");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}
