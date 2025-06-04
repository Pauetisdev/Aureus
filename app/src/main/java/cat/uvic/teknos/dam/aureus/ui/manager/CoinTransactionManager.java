package cat.uvic.teknos.dam.aureus.ui.manager;

import cat.uvic.teknos.dam.aureus.ModelFactory;
import cat.uvic.teknos.dam.aureus.repositories.RepositoryFactory;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class CoinTransactionManager {
    private final ModelFactory modelFactory;
    private final RepositoryFactory repositoryFactory;
    private final Scanner scanner;

    public CoinTransactionManager(ModelFactory modelFactory, RepositoryFactory repositoryFactory, Scanner scanner) {
        this.modelFactory = modelFactory;
        this.repositoryFactory = repositoryFactory;
        this.scanner = scanner;
    }

    public void run() {
        System.out.println("Coin Transaction Management, type:");
        System.out.println("1 - to show all coins in transactions");
        System.out.println("2 - to view coin transaction details");
        System.out.println("3 - to add coin to transaction");
        System.out.println("4 - to remove coin from transaction");
        System.out.println("5 - to exit");

        var repository = repositoryFactory.getCoinTransactionRepository();

        var command = "";
        while (!Objects.equals(command = scanner.nextLine(), "5")) {
            switch (command) {
                case "1":
                    var coinTransactions = repository.getAll();
                    System.out.println(AsciiTable.getTable(coinTransactions, Arrays.asList(
                            new Column().header("Coin ID").with(ct -> Integer.toString(ct.getCoin().getId())),
                            new Column().header("Transaction ID").with(ct -> Integer.toString(ct.getTransaction().getId())),
                            new Column().header("Coin").with(ct -> ct.getCoin().getCoinMaterial() + " - " + ct.getCoin().getCoinYear()),
                            new Column().header("Price").with(ct -> ct.getTransactionPrice() != null ? ct.getTransactionPrice().toString() : "N/A"),
                            new Column().header("Currency").with(ct -> ct.getCurrency() != null ? ct.getCurrency() : "EUR")
                    )));
                    break;

                case "2":
                    System.out.println("Enter coin ID:");
                    try {
                        var coinId = Integer.parseInt(scanner.nextLine());
                        System.out.println("Enter transaction ID:");
                        var transactionId = Integer.parseInt(scanner.nextLine());

                        var matchingTransaction = repository.getAll().stream()
                                .filter(ct -> ct.getCoin().getId() == coinId &&
                                        ct.getTransaction().getId() == transactionId)
                                .findFirst();

                        if (matchingTransaction.isPresent()) {
                            var ct = matchingTransaction.get();
                            System.out.println("Details found:");
                            System.out.println("Coin ID: " + ct.getCoin().getId());
                            System.out.println("Transaction ID: " + ct.getTransaction().getId());
                            System.out.println("Coin: " + ct.getCoin().getCoinMaterial() + " - " + ct.getCoin().getCoinYear());
                            System.out.println("Price: " + (ct.getTransactionPrice() != null ? ct.getTransactionPrice() : "N/A"));
                            System.out.println("Currency: " + (ct.getCurrency() != null ? ct.getCurrency() : "EUR"));
                        } else {
                            System.out.println("Not found");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID");
                    }
                    break;

                case "3":
                    try {
                        System.out.println("Transaction ID:");
                        var transactionId = Integer.parseInt(scanner.nextLine());
                        var transaction = repositoryFactory.getTransactionRepository().get(transactionId);

                        System.out.println("Coin ID:");
                        var coinId = Integer.parseInt(scanner.nextLine());
                        var coin = repositoryFactory.getCoinRepository().get(coinId);

                        if (transaction != null && coin != null) {
                            System.out.println("Price:");
                            var price = Double.parseDouble(scanner.nextLine());

                            System.out.println("Currency (leave empty for EUR):");
                            var currency = scanner.nextLine();

                            var coinTransaction = modelFactory.newCoinTransaction();
                            coinTransaction.setTransaction(transaction);
                            coinTransaction.setCoin(coin);
                            coinTransaction.setTransactionPrice(BigDecimal.valueOf(price));
                            coinTransaction.setCurrency(currency.isEmpty() ? "EUR" : currency);

                            repository.save(coinTransaction);
                            System.out.println("Coin successfully added to transaction");
                        } else {
                            System.out.println("Transaction or coin not found");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid value");
                    }
                    break;

                case "4":
                    try {
                        System.out.println("Enter coin ID:");
                        var coinId = Integer.parseInt(scanner.nextLine());
                        System.out.println("Enter transaction ID:");
                        var transactionId = Integer.parseInt(scanner.nextLine());

                        var matchingTransaction = repository.getAll().stream()
                                .filter(ct -> ct.getCoin().getId() == coinId &&
                                        ct.getTransaction().getId() == transactionId)
                                .findFirst();

                        if (matchingTransaction.isPresent()) {
                            repository.delete(matchingTransaction.get());
                            System.out.println("Successfully deleted");
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