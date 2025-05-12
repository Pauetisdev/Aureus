package cat.uvic.teknos.aureus.jdbc;

import java.sql.*;
import java.time.LocalDate;

public class JdbcApp {
    public static void main(String[] args) {
        try {
            executeLogic();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void executeLogic() throws SQLException {
        try (var connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/m0486", "root", "rootpassword")) {
            connection.setAutoCommit(false);
            System.out.println("🔗 Conectado al esquema: " + connection.getCatalog());

            // Añadir usuarios de ejemplo
            addUser(connection, "dgilmour", "david.gilmour@example.com", "pinkfloyd123", LocalDate.now());
            addUser(connection, "jhendrix", "jimi.hendrix@example.com", "purplehaze", LocalDate.now());

            connection.commit();

            // Consultar todos los usuarios
            var statement = connection.createStatement();
            var results = statement.executeQuery("SELECT * FROM USER");

            System.out.println("\nUsuarios en la tabla USER:");
            while (results.next()) {
                System.out.println("ID: " + results.getInt("USER_ID"));
                System.out.println("Username: " + results.getString("USERNAME"));
                System.out.println("Email: " + results.getString("EMAIL"));
                System.out.println("Join Date: " + results.getDate("JOIN_DATE"));
                System.out.println("-------------");
            }
        } catch (SQLException e) {
            System.err.println("Error durante la transacción. Rollback ejecutado.");
            e.printStackTrace();
        }
    }

    private static void addUser(Connection connection, String username, String email,
                                String passwordHash, LocalDate joinDate) throws SQLException {
        String sql = "INSERT INTO USER (USERNAME, EMAIL, PASSWORD_HASH, JOIN_DATE) VALUES (?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, passwordHash);
            preparedStatement.setDate(4, Date.valueOf(joinDate));

            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        System.out.println("Usuario insertado con ID generado: " + generatedId);
                    }
                }
            }
        }
    }
}