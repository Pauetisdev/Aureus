package cat.uvic.teknos.aureus.ui;

import cat.uvic.teknos.dam.aureus.ModelFactory;
import cat.uvic.teknos.dam.aureus.repositories.RepositoryFactory;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;

import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class UserManager {
    private ModelFactory modelFactory;
    private RepositoryFactory repositoryFactory;
    private Scanner scanner;

    public UserManager(ModelFactory modelFactory, RepositoryFactory repositoryFactory, Scanner scanner) {
        this.modelFactory = modelFactory;
        this.repositoryFactory = repositoryFactory;
        this.scanner = scanner;
    }

    public void run() {
        System.out.println("Instrument manager, type:");
        System.out.println("1 - to show the list of all the instruments");
        System.out.println("2 - to show an instrument");
        System.out.println("3 - to save a new instrument");
        System.out.println("4 - to delete an existing instrument");
        System.out.println("5 - to exit the instrument menu");

        var repository = repositoryFactory.getUserRepository();

        var command = "";
        while  (!Objects.equals(command = scanner.nextLine(), "5")) {
            switch (command) {
                case "1":
                    var instruments = repository.getAll();
                    System.out.println(AsciiTable.getTable(instruments, Arrays.asList(
                            new Column().with(i -> Integer.toString(i.getId())),
                            new Column().header("Username").with(i -> i.getUsername())
                    )));

                case "3":
                    System.out.println("Description:");

                    var instrument = modelFactory.newUser();

                    // TODO: validate input
                    var description = scanner.nextLine();
                    //instrument.setDescription(description);

                    repository.save(instrument);
                    break;
                default:
                    System.out.println("Invalid command");
                    break;
            }
        }
    }
}
