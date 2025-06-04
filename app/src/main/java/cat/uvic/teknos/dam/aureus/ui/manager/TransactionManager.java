package cat.uvic.teknos.dam.aureus.ui.manager;

import cat.uvic.teknos.dam.aureus.ModelFactory;
import cat.uvic.teknos.dam.aureus.repositories.RepositoryFactory;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class TransactionManager {
    private final ModelFactory modelFactory;
    private final RepositoryFactory repositoryFactory;
    private final Scanner scanner;

    public TransactionManager(ModelFactory modelFactory, RepositoryFactory repositoryFactory, Scanner scanner) {
        this.modelFactory = modelFactory;
        this.repositoryFactory = repositoryFactory;
        this.scanner = scanner;
    }

    private void displayAllTransactions() {
        var transactions = repositoryFactory.getTransactionRepository().getAll();
        if (transactions.isEmpty()) {
            System.out.println("No transactions available");
            return;
        }
        System.out.println(AsciiTable.getTable(transactions, Arrays.asList(
                new Column().header("ID").with(t -> Integer.toString(t.getId())),
                new Column().header("Date").with(t -> t.getTransactionDate().toString()),
                new Column().header("Buyer").with(t -> t.getBuyer().getUsername()),
                new Column().header("Seller").with(t -> t.getSeller().getUsername())
        )));
    }

    private void displayAvailableTransactionIds() {
        var transactions = repositoryFactory.getTransactionRepository().getAll();
        if (transactions.isEmpty()) {
            System.out.println("No transactions available");
            return;
        }
        System.out.println("\nAvailable transaction IDs:");
        transactions.stream()
                .map(t -> t.getId().toString())
                .sorted()
                .forEach(id -> System.out.print(id + " "));
        System.out.println("\n");
    }

    public void run() {
        while (true) {
            System.out.println("\nTransaction Management:");
            System.out.println("1 - View all transactions");
            System.out.println("2 - Search transaction by ID");
            System.out.println("3 - Search transactions by date range");
            System.out.println("4 - View coins in transaction");
            System.out.println("5 - Exit");

            var command = scanner.nextLine();

            if (Objects.equals(command, "5")) {
                break;
            }

            switch (command) {
                case "1":
                    displayAllTransactions();
                    break;

                case "2":
                    displayAvailableTransactionIds();
                    System.out.println("Enter transaction ID:");
                    try {
                        var id = Integer.parseInt(scanner.nextLine());
                        var transaction = repositoryFactory.getTransactionRepository().get(id);
                        if (transaction != null) {
                            System.out.println("\nTransaction details:");
                            System.out.println("----------------------------------------");
                            System.out.println("ID: " + transaction.getId());
                            System.out.println("Date: " + transaction.getTransactionDate());
                            System.out.println("Buyer: " + transaction.getBuyer().getUsername());
                            System.out.println("Seller: " + transaction.getSeller().getUsername());
                            System.out.println("----------------------------------------");
                        } else {
                            System.out.println("Transaction not found");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID");
                    }
                    break;

                case "3":
                    System.out.println("Enter start date (YYYY-MM-DD HH:MM:SS):");
                    var startStr = scanner.nextLine();
                    System.out.println("Enter end date (YYYY-MM-DD HH:MM:SS):");
                    var endStr = scanner.nextLine();
                    try {
                        var start = Timestamp.valueOf(startStr);
                        var end = Timestamp.valueOf(endStr);
                        var transactionsByDate = repositoryFactory.getTransactionRepository().findByDateRange(start, end);
                        if (!transactionsByDate.isEmpty()) {
                            System.out.println("\nTransactions found:");
                            System.out.println(AsciiTable.getTable(transactionsByDate, Arrays.asList(
                                    new Column().header("ID").with(t -> Integer.toString(t.getId())),
                                    new Column().header("Date").with(t -> t.getTransactionDate().toString()),
                                    new Column().header("Buyer").with(t -> t.getBuyer().getUsername()),
                                    new Column().header("Seller").with(t -> t.getSeller().getUsername())
                            )));
                        } else {
                            System.out.println("No transactions found in this date range");
                        }
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid date format");
                    }
                    break;

                case "4":
                    displayAvailableTransactionIds();
                    System.out.println("Enter transaction ID:");
                    try {
                        var transactionId = Integer.parseInt(scanner.nextLine());
                        var coinTransactions = repositoryFactory.getCoinTransactionRepository().getAll().stream()
                                .filter(ct -> ct.getTransaction().getId().equals(transactionId))
                                .toList();

                        if (coinTransactions.isEmpty()) {
                            System.out.println("No coins found in this transaction");
                        } else {
                            System.out.println("\nCoins in transaction:");
                            System.out.println(AsciiTable.getTable(coinTransactions, Arrays.asList(
                                    new Column().header("Coin ID").with(ct -> Integer.toString(ct.getCoin().getId())),
                                    new Column().header("Name").with(ct -> ct.getCoin().getCoinName()),
                                    new Column().header("Material").with(ct -> ct.getCoin().getCoinMaterial()),
                                    new Column().header("Year").with(ct -> Integer.toString(ct.getCoin().getCoinYear())),
                                    new Column().header("Country").with(ct -> ct.getCoin().getOriginCountry())
                            )));
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