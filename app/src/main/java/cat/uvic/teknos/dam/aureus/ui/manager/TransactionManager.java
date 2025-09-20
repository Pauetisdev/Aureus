package cat.uvic.teknos.dam.aureus.ui.manager;

import cat.uvic.teknos.dam.aureus.ModelFactory;
import cat.uvic.teknos.dam.aureus.User;
import cat.uvic.teknos.dam.aureus.Coin;
import cat.uvic.teknos.dam.aureus.repositories.RepositoryFactory;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.EntityNotFoundException;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.RepositoryException;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages transaction operations in the AUREUS system.
 * Provides functionality for viewing and searching transactions,
 * as well as viewing associated coin details.
 *
 * @author Pau
 * @version 1.1
 */
public class TransactionManager {
    private final ModelFactory modelFactory;
    private final RepositoryFactory repositoryFactory;
    private final Scanner scanner;

    public TransactionManager(ModelFactory modelFactory, RepositoryFactory repositoryFactory, Scanner scanner) {
        this.modelFactory = modelFactory;
        this.repositoryFactory = repositoryFactory;
        this.scanner = scanner;
    }

    /**
     * Devuelve solo los usuarios que tienen al menos una colección con monedas.
     */
    private List<User> getUsersWithCollections() {
        var allUsers = repositoryFactory.getUserRepository().getAll();
        var allCoinCollections = repositoryFactory.getCoinCollectionRepository().getAll();

        return allUsers.stream()
                .filter(user -> allCoinCollections.stream()
                        .anyMatch(cc -> cc.getCollection() != null
                                && cc.getCollection().getUser() != null
                                && cc.getCollection().getUser().getId().equals(user.getId())
                                && cc.getCoin() != null))
                .collect(Collectors.toList());
    }

    /**
     * Handles the creation of a new transaction and the association of coins to it.
     */
    private void handleCreateTransaction() {
        try {
            // --- Obtener buyers con colecciones que tengan monedas ---
            var buyers = getUsersWithCollections();
            if (buyers.isEmpty()) {
                System.out.println("No buyers with collections containing coins available.");
                return;
            }

            System.out.println("\nAvailable buyers (must have collections with coins):");
            System.out.println("+----+----------+");
            System.out.println("| ID | Username |");
            System.out.println("+----+----------+");
            buyers.forEach(u -> System.out.printf("| %2d | %-8s |%n", u.getId(), u.getUsername()));
            System.out.println("+----+----------+");

            System.out.print("Enter Buyer ID: ");
            var buyerId = Integer.parseInt(scanner.nextLine());
            var buyer = repositoryFactory.getUserRepository().get(buyerId);

            // Verificar que el buyer existe y está en la lista de buyers válidos
            boolean buyerValid = buyer != null && buyers.stream().anyMatch(u -> u.getId().equals(buyerId));
            if (!buyerValid) {
                System.out.println("Buyer not found or has no collections with coins");
                return;
            }

            // --- Obtener sellers con colecciones que tengan monedas, excluyendo buyer ---
            var sellers = getUsersWithCollections().stream()
                    .filter(u -> !u.getId().equals(buyer.getId()))
                    .collect(Collectors.toList());

            if (sellers.isEmpty()) {
                System.out.println("No sellers with collections containing coins available.");
                return;
            }

            System.out.println("\nAvailable sellers (must have collections with coins):");
            System.out.println("+----+----------+");
            System.out.println("| ID | Username |");
            System.out.println("+----+----------+");
            sellers.forEach(u -> System.out.printf("| %2d | %-8s |%n", u.getId(), u.getUsername()));
            System.out.println("+----+----------+");

            System.out.print("Enter Seller ID: ");
            var sellerId = Integer.parseInt(scanner.nextLine());
            var seller = repositoryFactory.getUserRepository().get(sellerId);

            // Verificar que el seller exists y está en la lista de sellers válidos
            boolean sellerValid = seller != null && sellers.stream().anyMatch(u -> u.getId().equals(sellerId));
            if (!sellerValid) {
                System.out.println("Seller not found or has no collections with coins");
                return;
            }

            // --- Elegir moneda del vendedor ---
            var sellerCoins = repositoryFactory.getCoinCollectionRepository().getAll().stream()
                    .filter(cc -> cc.getCollection().getUser().getId().equals(seller.getId()))
                    .map(cc -> cc.getCoin())
                    .collect(Collectors.toList());

            if (sellerCoins.isEmpty()) {
                System.out.println("Seller has no coins to sell. Transaction cannot proceed.");
                return;
            }

            System.out.println("\nAvailable coins from seller:");
            System.out.println("+----+---------------+---------------+------+");
            System.out.println("| ID | Name          | Material      | Year |");
            System.out.println("+----+---------------+---------------+------+");
            sellerCoins.forEach(c -> System.out.printf("| %2d | %-13s | %-13s | %4d |%n",
                    c.getId(), c.getCoinName(), c.getCoinMaterial(), c.getCoinYear()));
            System.out.println("+----+---------------+---------------+------+");

            System.out.print("Enter Coin ID: ");
            var coinId = Integer.parseInt(scanner.nextLine());
            var coin = sellerCoins.stream().filter(c -> c.getId().equals(coinId)).findFirst().orElse(null);
            if (coin == null) {
                System.out.println("Coin not found in seller's collection");
                return;
            }

            System.out.print("Enter transaction price: ");
            var price = new BigDecimal(scanner.nextLine());
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("Price must be greater than 0");
                return;
            }

            // --- CREAR Y CONFIGURAR LA TRANSACCIÓN COMPLETAMENTE ANTES DE GUARDAR ---
            var transaction = modelFactory.newTransaction();
            transaction.setBuyer(buyer);
            transaction.setSeller(seller);
            transaction.setTransactionDate(new Timestamp(System.currentTimeMillis()));

            // --- GUARDAR LA TRANSACCIÓN PRIMERO ---
            repositoryFactory.getTransactionRepository().save(transaction);

            // Verificar que se guardó correctamente verificando que tenga ID
            if (transaction.getId() == null) {
                System.out.println("Error: Transaction could not be saved - no ID assigned");
                return;
            }

            // --- CREAR Y GUARDAR LA RELACIÓN COIN-TRANSACTION ---
            var coinTransaction = modelFactory.newCoinTransaction();
            coinTransaction.setTransaction(transaction); // Usar la transacción que ya tiene ID
            coinTransaction.setCoin(coin);
            coinTransaction.setTransactionPrice(price);
            coinTransaction.setCurrency("EUR");

            repositoryFactory.getCoinTransactionRepository().save(coinTransaction);

            System.out.println("Transaction created successfully with ID: " + transaction.getId());
            System.out.println("Coin added to transaction: " + coin.getCoinName() + " at price " + price + " EUR");

        } catch (NumberFormatException e) {
            System.out.println("Invalid numeric input: " + e.getMessage());
        } catch (RepositoryException e) {
            System.out.println("Database error creating transaction: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error creating transaction: " + e.getMessage());
            e.printStackTrace(); // Para debugging
        }
    }
    // ------------------ RESTO DEL MENÚ ------------------ //

    private void displayAllTransactions() {
        try {
            var transactions = repositoryFactory.getTransactionRepository().getAll();
            if (transactions.isEmpty()) {
                System.out.println("No transactions available");
                return;
            }

            System.out.println("\nAll Transactions:");
            System.out.println(AsciiTable.getTable(transactions, Arrays.asList(
                    new Column().header("ID").with(t -> t.getId().toString()),
                    new Column().header("Date").with(t -> t.getTransactionDate().toString())
            )));

        } catch (RepositoryException e) {
            System.out.println("Error accessing transactions: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayAvailableTransactionIds() {
        var transactions = repositoryFactory.getTransactionRepository().getAll();
        if (transactions.isEmpty()) {
            System.out.println("No transactions available");
            return;
        }

        System.out.println("\nAvailable Transactions:");
        System.out.println(AsciiTable.getTable(transactions, Arrays.asList(
                new Column().header("ID").with(t -> t.getId().toString()),
                new Column().header("Date").with(t -> t.getTransactionDate().toString()),
                new Column().header("Buyer").with(t -> t.getBuyer().getUsername()),
                new Column().header("Seller").with(t -> t.getSeller().getUsername())
        )));
    }

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

    private void handleSearchById() {
        try {
            var transactions = repositoryFactory.getTransactionRepository().getAll();
            if (transactions.isEmpty()) {
                System.out.println("No transactions available");
                return;
            }
            displayAvailableTransactionIds();

            System.out.print("Enter transaction ID: ");
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
        } catch (EntityNotFoundException e) {
            System.out.println("Error: Transaction has invalid buyer or seller. " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID");
        } catch (RepositoryException e) {
            System.out.println("Error accessing transactions: " + e.getMessage());
        }
    }


    private void handleDateRangeSearch() {
        var transactions = repositoryFactory.getTransactionRepository().getAll();
        if (transactions.isEmpty()) {
            System.out.println("No transactions available");
            return;
        }

        System.out.println("Enter start date (YYYY-MM-DD):");
        var startStr = scanner.nextLine();
        System.out.println("Enter end date (YYYY-MM-DD):");
        var endStr = scanner.nextLine();

        try {
            // Agregar automáticamente 00:00:00 al inicio y 23:59:59 al final
            var start = Timestamp.valueOf(startStr + " 00:00:00");
            var end = Timestamp.valueOf(endStr + " 23:59:59");
            displayTransactionsByDateRange(start, end);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid date format. Please use YYYY-MM-DD (example: 2025-09-20)");
        }
    }

    private void displayTransactionsByDateRange(Timestamp start, Timestamp end) {
        try {
            var transactionsByDate = repositoryFactory.getTransactionRepository().findByDateRange(start, end);
            if (!transactionsByDate.isEmpty()) {
                System.out.println("\nTransactions found:");
                System.out.println(AsciiTable.getTable(transactionsByDate, Arrays.asList(
                        new Column().header("ID").with(t -> t.getId().toString()),
                        new Column().header("Date").with(t -> t.getTransactionDate().toString().split(" ")[0]), // Solo mostrar fecha
                        new Column().header("Time").with(t -> t.getTransactionDate().toString().split(" ")[1]), // Solo mostrar hora
                        new Column().header("Buyer").with(t -> t.getBuyer().getUsername()),
                        new Column().header("Seller").with(t -> t.getSeller().getUsername())
                )));
            } else {
                System.out.println("No transactions found between " + start.toString().split(" ")[0] + " and " + end.toString().split(" ")[0]);
            }
        } catch (EntityNotFoundException e) {
            System.out.println("Error: Transaction has invalid buyer or seller. " + e.getMessage());
        } catch (RepositoryException e) {
            System.out.println("Error accessing transactions: " + e.getMessage());
        }
    }


    private void handleViewTransactionCoins() {
        var transactions = repositoryFactory.getTransactionRepository().getAll();

        if (transactions.isEmpty()) {
            System.out.println("No transactions available");
            return;
        }

        System.out.println("\nAvailable Transactions:");
        System.out.println(AsciiTable.getTable(transactions, Arrays.asList(
                new Column().header("ID").with(t -> t.getId().toString()),
                new Column().header("Date").with(t -> t.getTransactionDate().toString().split(" ")[0]),
                new Column().header("Buyer").with(t -> t.getBuyer().getUsername()),
                new Column().header("Seller").with(t -> t.getSeller().getUsername())
        )));

        System.out.print("\nEnter transaction ID to view coins: ");
        try {
            var transactionId = Integer.parseInt(scanner.nextLine());
            displayCoinsInTransaction(transactionId);
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID");
        }
    }

    private void displayCoinsInTransaction(Integer transactionId) {
        var coinTransactions = repositoryFactory.getCoinTransactionRepository().getAll().stream()
                .filter(ct -> ct.getTransaction().getId().equals(transactionId))
                .toList();

        if (coinTransactions.isEmpty()) {
            System.out.println("No coins found in this transaction");
        } else {
            System.out.println("\nCoins in Transaction ID " + transactionId + ":");
            System.out.println(AsciiTable.getTable(coinTransactions, Arrays.asList(
                    new Column().header("Coin ID").with(ct -> ct.getCoin().getId().toString()),
                    new Column().header("Name").with(ct -> ct.getCoin().getCoinName()),
                    new Column().header("Material").with(ct -> ct.getCoin().getCoinMaterial()),
                    new Column().header("Year").with(ct -> ct.getCoin().getCoinYear().toString()),
                    new Column().header("Country").with(ct -> ct.getCoin().getOriginCountry()),
                    new Column().header("Price").with(ct -> ct.getTransactionPrice().toString() + " " + ct.getCurrency())
            )));
        }
    }
}
