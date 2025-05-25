package cat.uvic.teknos.dam.aureus.repositories.jdbc;

import cat.uvic.teknos.dam.aureus.User;
import cat.uvic.teknos.dam.aureus.impl.UserImpl;
import cat.uvic.teknos.dam.aureus.repositories.UserRepository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.DataSource;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.CrudException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class JdbcUserRepository implements UserRepository {
    private final DataSource dataSource;

    public JdbcUserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(User user) {
        String sql = "INSERT INTO USER (USERNAME, EMAIL, PASSWORD_HASH) VALUES (?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new CrudException("Error al guardar el usuario", e);
        }
    }

    @Override
    public void delete(User user) {
        String sql = "DELETE FROM USER WHERE USER_ID = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, user.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new CrudException("Error al borrar el usuario", e);
        }
    }

    @Override
    public User get(Integer id) {
        String sql = "SELECT * FROM USER WHERE USER_ID = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new UserImpl();
                    user.setId(rs.getInt("USER_ID"));
                    user.setUsername(rs.getString("USERNAME"));
                    user.setEmail(rs.getString("EMAIL"));
                    user.setPasswordHash(rs.getString("PASSWORD_HASH"));
                    return user;
                }
                return null;
            }

        } catch (SQLException e) {
            throw new CrudException("Error al obtener el usuario", e);
        }
    }

    @Override
    public Set<User> getAll() {
        String sql = "SELECT * FROM USER";
        Set<User> users = new HashSet<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                User user = new UserImpl();
                user.setId(rs.getInt("USER_ID"));
                user.setUsername(rs.getString("USERNAME"));
                user.setEmail(rs.getString("EMAIL"));
                user.setPasswordHash(rs.getString("PASSWORD_HASH"));
                users.add(user);
            }
            return users;

        } catch (SQLException e) {
            throw new CrudException("Error al obtener todos los usuarios", e);
        }
    }

    public User findByEmail(String email) {
        String sql = "SELECT * FROM USER WHERE EMAIL = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new UserImpl();
                    user.setId(rs.getInt("USER_ID"));
                    user.setUsername(rs.getString("USERNAME"));
                    user.setEmail(rs.getString("EMAIL"));
                    user.setPasswordHash(rs.getString("PASSWORD_HASH"));
                    return user;
                }
                return null;
            }
        } catch (SQLException e) {
            throw new CrudException("Error al obtener el usuario por email", e);
        }
    }
}
