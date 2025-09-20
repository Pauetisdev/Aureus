package cat.uvic.teknos.dam.aureus.ui.manager;

import cat.uvic.teknos.dam.aureus.ModelFactory;
import cat.uvic.teknos.dam.aureus.repositories.RepositoryFactory;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import cat.uvic.teknos.dam.aureus.Coin;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;
import java.util.ArrayList;


/**
 * Manages transaction operations in the AUREUS system.
 * Provides functionality for viewing and searching transactions,
 * as well as viewing associated coin details.
 *
 * @author Pau Vilardell
 * @version 1.0
 * @since 2025-06-05
 */
public class TransactionManager {
    private final ModelFactory modelFactory;
    private final RepositoryFactory repositoryFactory;
    private final Scanner scanner;

    /**
     * Constructs a new TransactionManager with the necessary dependencies.
     *
     * @param modelFactory Factory for creating domain model objects
     * @param repositoryFactory Factory for accessing data repositories
     * @param scanner Scanner for reading user input
     */
    public TransactionManager(ModelFactory modelFactory, RepositoryFactory repositoryFactory, Scanner scanner) {
        this.modelFactory = modelFactory;
        this.repositoryFactory = repositoryFactory;
        this.scanner = scanner;
    }

    /**
     * Handles the creation of a new transaction and the association of coins to it.
     * The user is prompted to select a buyer, seller, and coins to include in the transaction.
     */
    private void handleCreateTransaction() {
        try {
            var transaction = modelFactory.newTransaction();

            // Seleccionar comprador
            System.out.println("\nAvailable users:");
            repositoryFactory.getUserRepository().getAll().forEach(u ->
                    System.out.printf("ID: %d, Username: %s%n", u.getId(), u.getUsername())
            );
            System.out.print("Enter Buyer ID: ");
            var buyerId = Integer.parseInt(scanner.nextLine());
            var buyer = repositoryFactory.getUserRepository().get(buyerId);
            if (buyer == null) { System.out.println("Buyer not found"); return; }
            transaction.setBuyer(buyer);

            // Seleccionar vendedor
            System.out.println("\nAvailable users:");
            repositoryFactory.getUserRepository().getAll().forEach(u ->
                    System.out.printf("ID: %d, Username: %s%n", u.getId(), u.getUsername())
            );
            System.out.print("Enter Seller ID: ");
            var sellerId = Integer.parseInt(scanner.nextLine());
            var seller = repositoryFactory.getUserRepository().get(sellerId);
            if (seller == null) { System.out.println("Seller not found"); return; }
            transaction.setSeller(seller);

            transaction.setTransactionDate(new Timestamp(System.currentTimeMillis()));
            repositoryFactory.getTransactionRepository().save(transaction);
            System.out.println("Transaction created with ID: " + transaction.getId());

            // Obtener monedas del vendedor usando CoinCollection
            var allCoinCollections = repositoryFactory.getCoinCollectionRepository().getAll();
            var sellerCoins = new ArrayList<Coin>();

            for (var cc : allCoinCollections) {
                var collection = cc.getCollection();
                if (collection != null && collection.getUser().equals(seller.getId())) {
                    sellerCoins.add(cc.getCoin());
                }
            }

            if (sellerCoins.isEmpty()) {
                System.out.println("Seller has no coins to sell");
                return;
            }

            // Elegir monedas para la transacción
            while (true) {
                System.out.println("\nAdd a coin to the transaction? (y/n)");
                var answer = scanner.nextLine().trim().toLowerCase();
                if (!answer.equals("y")) break;

                System.out.println("\nAvailable coins from seller:");
                for (var c : sellerCoins) {
                    System.out.printf("ID: %d, Name: %s, Material: %s, Year: %d%n",
                            c.getId(), c.getCoinName(), c.getCoinMaterial(), c.getCoinYear());
                }

                System.out.print("Enter Coin ID: ");
                var coinId = Integer.parseInt(scanner.nextLine());
                var coin = sellerCoins.stream().filter(c -> c.getId().equals(coinId)).findFirst().orElse(null);
                if (coin == null) { System.out.println("Coin not found"); continue; }

                System.out.print("Enter transaction price: ");
                var price = new BigDecimal(scanner.nextLine());

                var coinTransaction = modelFactory.newCoinTransaction();
                coinTransaction.setTransaction(transaction);
                coinTransaction.setCoin(coin);
                coinTransaction.setTransactionPrice(price);
                coinTransaction.setCurrency("EUR");

                repositoryFactory.getCoinTransactionRepository().save(coinTransaction);
                System.out.println("Coin added to transaction.");

                sellerCoins.remove(coin); // Evita vender la misma moneda dos veces
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid input");
        } catch (Exception e) {
            System.out.println("Error creating transaction: " + e.getMessage());
        }
    }



    /**
     * Displays all transactions in a formatted table.
     * If no transactions exist, displays an appropriate message.
     */
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

    /**
     * Displays a list of all available transaction IDs.
     * Used to help users select valid transaction IDs for other operations.
     */
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

    /**
     * Runs the main transaction management interface.
     * Provides a menu-driven interface for all transaction-related operations:
     * - View all transactions
     * - Search transaction by ID
     * - Search transactions by date range
     * - View coins in transaction
     */
    public void run() {
        while (true) {
            System.out.println("\nTransaction Management:");
            System.out.println("1 - View all transactions");
            System.out.println("2 - Search transaction by ID");
            System.out.println("3 - Create new transaction");
            System.out.println("4 - Search transactions by date range");
            System.out.println("5 - View coins in transaction");
            System.out.println("6 - Exit");

            System.out.print("Select an option: ");
            var command = scanner.nextLine();

            if (Objects.equals(command, "6")) {
                break;
            }

            try {
                switch (command) {
                    case "1" -> displayAllTransactions();
                    case "2" -> handleSearchById();
                    case "3" -> handleCreateTransaction();
                    case "4" -> handleDateRangeSearch();
                    case "5" -> handleViewTransactionCoins();
                    default -> System.out.println("Invalid command");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    /**
     * Handles the search transaction by ID functionality.
     * Displays detailed information about a specific transaction.
     */
    private void handleSearchById() {
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

        System.out.print("Enter transaction ID: ");
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
    }


    /**
     * Handles the search transactions by date range functionality.
     * Allows users to search for transactions within a specific time period.
     */
    private void handleDateRangeSearch() {
        var transactions = repositoryFactory.getTransactionRepository().getAll();
        if (transactions.isEmpty()) {
            System.out.println("No transactions available");
            return;
        }

        System.out.println("Enter start date (YYYY-MM-DD HH:MM:SS):");
        var startStr = scanner.nextLine();
        System.out.println("Enter end date (YYYY-MM-DD HH:MM:SS):");
        var endStr = scanner.nextLine();
        try {
            var start = Timestamp.valueOf(startStr);
            var end = Timestamp.valueOf(endStr);
            displayTransactionsByDateRange(start, end);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid date format");
        }
    }


    /**
     * Displays transactions that occurred within the specified date range.
     *
     * @param start Start date/time for the search range
     * @param end End date/time for the search range
     */
    private void displayTransactionsByDateRange(Timestamp start, Timestamp end) {
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
    }

    /**
     * Handles the view coins in transaction functionality.
     * Shows details of all coins involved in a specific transaction.
     */
    private void handleViewTransactionCoins() {
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

        System.out.print("Enter transaction ID: ");
        try {
            var transactionId = Integer.parseInt(scanner.nextLine());
            displayCoinsInTransaction(transactionId);
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID");
        }
    }


    /**
     * Displays all coins associated with a specific transaction.
     *
     * @param transactionId ID of the transaction whose coins should be displayed
     */
    private void displayCoinsInTransaction(Integer transactionId) {
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
    }
}