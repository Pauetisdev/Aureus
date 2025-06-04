package cat.uvic.teknos.dam.aureus.repositories.jdbc;

import cat.uvic.teknos.dam.aureus.User;
import cat.uvic.teknos.dam.aureus.impl.UserImpl;
import cat.uvic.teknos.dam.aureus.repositories.UserRepository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.DataSource;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.CrudException;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

public class JdbcUserRepository implements UserRepository {
    private final DataSource dataSource;

    public JdbcUserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(User user) {
        String sql = """
            INSERT INTO USER (USERNAME, EMAIL, PASSWORD_HASH, JOIN_DATE)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                USERNAME = VALUES(USERNAME),
                PASSWORD_HASH = VALUES(PASSWORD_HASH),
                JOIN_DATE = VALUES(JOIN_DATE)
            """;

        try (var connection = dataSource.getConnection();
             var ps = connection.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setTimestamp(4, Timestamp.valueOf(user.getJoinDate()));

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new CrudException("Error al guardar el usuario", e);
        }
    }

    @Override
    public void delete(User user) {
        String sql = "DELETE FROM USER WHERE USER_ID = ?";
        try (var connection = dataSource.getConnection();
             var ps = connection.prepareStatement(sql)) {

            ps.setInt(1, user.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new CrudException("Error al borrar el usuario", e);
        }
    }

    @Override
    public User get(Integer id) {
        String sql = "SELECT * FROM USER WHERE USER_ID = ?";
        try (var connection = dataSource.getConnection();
             var ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildUserFromResultSet(rs);
                }
            }

        } catch (SQLException e) {
            throw new CrudException("Error al obtener el usuario", e);
        }
        return null;
    }

    @Override
    public Set<User> getAll() {
        String sql = "SELECT * FROM USER";
        Set<User> users = new HashSet<>();

        try (var connection = dataSource.getConnection();
             var ps = connection.prepareStatement(sql);
             var rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(buildUserFromResultSet(rs));
            }

        } catch (SQLException e) {
            throw new CrudException("Error al obtener todos los usuarios", e);
        }

        return users;
    }

    @Override
    public User findByEmail(String email) {
        String sql = "SELECT * FROM USER WHERE EMAIL = ?";
        try (var connection = dataSource.getConnection();
             var ps = connection.prepareStatement(sql)) {

            ps.setString(1, email);

            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildUserFromResultSet(rs);
                }
            }

        } catch (SQLException e) {
            throw new CrudException("Error al obtener el usuario por email", e);
        }

        return null;
    }

    private User buildUserFromResultSet(java.sql.ResultSet rs) throws SQLException {
        User user = new UserImpl();
        user.setId(rs.getInt("USER_ID"));
        user.setUsername(rs.getString("USERNAME"));
        user.setEmail(rs.getString("EMAIL"));
        user.setPasswordHash(rs.getString("PASSWORD_HASH"));

        Timestamp joinDate = rs.getTimestamp("JOIN_DATE");
        if (joinDate != null) {
            user.setJoinDate(joinDate.toLocalDateTime());
        }

        return user;
    }
}