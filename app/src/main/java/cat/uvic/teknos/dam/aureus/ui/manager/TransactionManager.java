package cat.uvic.teknos.dam.aureus.ui.manager;

import cat.uvic.teknos.dam.aureus.ModelFactory;
import cat.uvic.teknos.dam.aureus.repositories.RepositoryFactory;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;

import java.sql.Timestamp;
import java.time.LocalDateTime;
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

    public void run() {
        System.out.println("Transaction Management, type:");
        System.out.println("1 - to show all transactions");
        System.out.println("2 - to search transactions by date range");
        System.out.println("3 - to create new transaction");
        System.out.println("4 - to delete a transaction");
        System.out.println("5 - to exit");

        var repository = repositoryFactory.getTransactionRepository();

        var command = "";
        while (!Objects.equals(command = scanner.nextLine(), "5")) {
            switch (command) {
                case "1":
                    var transactions = repository.getAll();
                    System.out.println(AsciiTable.getTable(transactions, Arrays.asList(
                            new Column().header("ID").with(t -> Integer.toString(t.getId())),
                            new Column().header("Date").with(t -> t.getTransactionDate().toString()),
                            new Column().header("Buyer").with(t -> t.getBuyer().getUsername()),
                            new Column().header("Seller").with(t -> t.getSeller().getUsername())
                    )));
                    break;

                case "2":
                    System.out.println("Enter start date (YYYY-MM-DD HH:MM:SS):");
                    var startStr = scanner.nextLine();
                    System.out.println("Enter end date (YYYY-MM-DD HH:MM:SS):");
                    var endStr = scanner.nextLine();
                    try {
                        var start = Timestamp.valueOf(startStr);
                        var end = Timestamp.valueOf(endStr);
                        var transactionsByDate = repository.findByDateRange(start, end);
                        System.out.println(AsciiTable.getTable(transactionsByDate, Arrays.asList(
                                new Column().header("ID").with(t -> Integer.toString(t.getId())),
                                new Column().header("Date").with(t -> t.getTransactionDate().toString()),
                                new Column().header("Buyer").with(t -> t.getBuyer().getUsername()),
                                new Column().header("Seller").with(t -> t.getSeller().getUsername())
                        )));
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid date format");
                    }
                    break;

                case "3":
                    try {
                        var userRepo = repositoryFactory.getUserRepository();

                        System.out.println("Buyer ID:");
                        var buyerId = Integer.parseInt(scanner.nextLine());
                        var buyer = userRepo.get(buyerId);

                        System.out.println("Seller ID:");
                        var sellerId = Integer.parseInt(scanner.nextLine());
                        var seller = userRepo.get(sellerId);

                        if (buyer != null && seller != null) {
                            var transaction = modelFactory.newTransaction();
                            transaction.setBuyer(buyer);
                            transaction.setSeller(seller);
                            transaction.setTransactionDate(Timestamp.valueOf(LocalDateTime.now()));

                            repository.save(transaction);
                            System.out.println("Transaction successfully saved");
                        } else {
                            System.out.println("Buyer or seller not found");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID");
                    }
                    break;

                case "4":
                    System.out.println("Enter transaction ID to delete:");
                    try {
                        var id = Integer.parseInt(scanner.nextLine());
                        var transactionToDelete = repository.get(id);
                        if (transactionToDelete != null) {
                            repository.delete(transactionToDelete);
                            System.out.println("Transaction successfully deleted");
                        } else {
                            System.out.println("Transaction not found");
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