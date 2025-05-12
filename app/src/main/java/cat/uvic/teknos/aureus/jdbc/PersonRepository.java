package cat.uvic.teknos.aureus.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PersonRepository {
    private Connection connection;

    public PersonRepository(Connection connection) {
        this.connection = connection;
    }

    public void save(Person person) throws SQLException {
        connection.setAutoCommit(false);

        try {
            if(person.getId() <= 0) {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "INSERT INTO person (first_name, last_name) VALUES (?, ?)");
                preparedStatement.setString(1, person.getFirstName());
                preparedStatement.setString(2, person.getLastName());
                preparedStatement.executeUpdate();
            } else {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "UPDATE person SET first_name = ?, last_name = ? WHERE id = ?");
                preparedStatement.setString(1, person.getFirstName());
                preparedStatement.setString(2, person.getLastName());
                preparedStatement.setInt(3, person.getId());
                preparedStatement.executeUpdate();
            }
            connection.commit();
        } catch(SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public void delete(Person person) {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/m0d86", "root", "rootpassword")) {

            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "DELETE FROM person WHERE id = ?");
            preparedStatement.setInt(1, person.getId());
            preparedStatement.executeUpdate();
            connection.commit();

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Person get(int id) throws SQLException {
        String sql = "SELECT * FROM person WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Person person = new Person();
                    person.setId(rs.getInt("id"));
                    person.setFirstName(rs.getString("first_name"));
                    person.setLastName(rs.getString("last_name"));
                    // Si hay más campos en tu tabla, los añadirías aquí
                    return person;
                }
            }
        }
        return null; // Si no encuentra el registro
    }

    public List<Person> getAll() throws SQLException {
        List<Person> persons = new ArrayList<>();
        String sql = "SELECT * FROM person";

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                Person person = new Person();
                person.setId(rs.getInt("id"));
                person.setFirstName(rs.getString("first_name"));
                person.setLastName(rs.getString("last_name"));
                // Añadir aquí otros campos si existen

                persons.add(person);
            }
        }
        return persons;
    }
}