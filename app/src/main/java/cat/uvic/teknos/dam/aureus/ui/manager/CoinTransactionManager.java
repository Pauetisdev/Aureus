package cat.uvic.teknos.dam.aureus.ui.manager;

import cat.uvic.teknos.dam.aureus.ModelFactory;
import cat.uvic.teknos.dam.aureus.repositories.RepositoryFactory;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;

import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

/**
 * Manages the visualization of coin transactions in the system.
 * This class provides a read-only interface to view coin transaction information.
 */
public class CoinTransactionManager {
    private final ModelFactory modelFactory;
    private final RepositoryFactory repositoryFactory;
    private final Scanner scanner;

    /**
     * Constructs a new CoinTransactionManager with the necessary dependencies.
     *
     * @param modelFactory Factory for creating domain model objects
     * @param repositoryFactory Factory for accessing data repositories
     * @param scanner Scanner for reading user input
     */
    public CoinTransactionManager(ModelFactory modelFactory, RepositoryFactory repositoryFactory, Scanner scanner) {
        this.modelFactory = modelFactory;
        this.repositoryFactory = repositoryFactory;
        this.scanner = scanner;
    }

    /**
     * Runs the main interface for viewing coin transactions.
     */
    public void run() {
        while (true) {
            // Display main menu
            System.out.println("\n=== Coin Transaction Management ===");
            System.out.println("1 - Show all coins in transactions");
            System.out.println("2 - View specific transaction details");
            System.out.println("3 - Exit");
            System.out.print("\nEnter your choice: ");

            var repository = repositoryFactory.getCoinTransactionRepository();
            var command = scanner.nextLine();

            if (Objects.equals(command, "3")) {
                break;
            }

            try {
                switch (command) {
                    case "1" -> {
                        System.out.println("\nList of all coin transactions:");
                        var coinTransactions = repository.getAll();
                        if (coinTransactions.isEmpty()) {
                            System.out.println("No transactions found");
                            continue;
                        }

                        System.out.println(AsciiTable.getTable(coinTransactions, Arrays.asList(
                                new Column().header("Coin ID").with(ct -> Integer.toString(ct.getCoin().getId())),
                                new Column().header("Transaction ID").with(ct -> Integer.toString(ct.getTransaction().getId())),
                                new Column().header("Coin").with(ct -> ct.getCoin().getCoinMaterial() + " - " + ct.getCoin().getCoinYear()),
                                new Column().header("Price").with(ct -> ct.getTransactionPrice() != null ? ct.getTransactionPrice().toString() : "N/A"),
                                new Column().header("Currency").with(ct -> ct.getCurrency() != null ? ct.getCurrency() : "EUR")
                        )));
                    }

                    case "2" -> viewTransactionDetails();
                    default -> System.out.println("\nInvalid command. Please try again.");
                }
            } catch (Exception e) {
                System.out.println("\nError: " + e.getMessage());
            }

            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }

    /**
     * Shows detailed information about a specific coin transaction.
     */
    private void viewTransactionDetails() {
        System.out.print("\nEnter coin ID: ");
        var coinId = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter transaction ID: ");
        var transactionId = Integer.parseInt(scanner.nextLine());

        var repository = repositoryFactory.getCoinTransactionRepository();
        var matchingTransaction = repository.getAll().stream()
                .filter(ct -> ct.getCoin().getId() == coinId &&
                        ct.getTransaction().getId() == transactionId)
                .findFirst();

        System.out.println(); // Blank line for better readability

        if (matchingTransaction.isPresent()) {
            var ct = matchingTransaction.get();
            System.out.println("Transaction Details:");
            System.out.println("------------------");
            System.out.println("Coin ID: " + ct.getCoin().getId());
            System.out.println("Transaction ID: " + ct.getTransaction().getId());
            System.out.println("Coin: " + ct.getCoin().getCoinMaterial() + " - " + ct.getCoin().getCoinYear());
            System.out.println("Price: " + (ct.getTransactionPrice() != null ? ct.getTransactionPrice() : "N/A"));
            System.out.println("Currency: " + (ct.getCurrency() != null ? ct.getCurrency() : "EUR"));
        } else {
            System.out.println("Transaction not found");
        }
    }
}