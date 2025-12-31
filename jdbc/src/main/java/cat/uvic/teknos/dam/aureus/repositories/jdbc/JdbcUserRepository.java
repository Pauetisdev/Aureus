package cat.uvic.teknos.dam.aureus.repositories.jdbc;

import cat.uvic.teknos.dam.aureus.User;
import cat.uvic.teknos.dam.aureus.impl.UserImpl;
import cat.uvic.teknos.dam.aureus.repositories.UserRepository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.DataSource;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.CrudException;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class JdbcUserRepository implements UserRepository {
    private final DataSource dataSource;

    public JdbcUserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(User user) {
        String updateSql = "UPDATE `USER` SET USERNAME = ?, EMAIL = ?, PASSWORD_HASH = ?, JOIN_DATE = ? WHERE USER_ID = ?";
        String insertSql = "INSERT INTO `USER` (USERNAME, EMAIL, PASSWORD_HASH, JOIN_DATE) VALUES (?, ?, ?, ?)";


        try (var connection = dataSource.getConnection()) {
            int affectedRows = 0;
            if (user.getId() != null) {
                try (var ps = connection.prepareStatement(updateSql)) {
                    ps.setString(1, user.getUsername());
                    ps.setString(2, user.getEmail());
                    ps.setString(3, user.getPasswordHash());
                    if (user.getJoinDate() != null) {
                        ps.setTimestamp(4, Timestamp.valueOf(user.getJoinDate()));
                    } else {
                        ps.setNull(4, java.sql.Types.TIMESTAMP);
                    }
                    ps.setInt(5, user.getId());
                    affectedRows = ps.executeUpdate();
                }
                if (affectedRows > 0) return;
            }
            try (var ps = connection.prepareStatement(insertSql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getEmail());
                ps.setString(3, user.getPasswordHash());
                if (user.getJoinDate() != null) {
                    ps.setTimestamp(4, Timestamp.valueOf(user.getJoinDate()));
                } else {
                    ps.setNull(4, java.sql.Types.TIMESTAMP);
                }
                ps.executeUpdate();
                try (var rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new CrudException("Error al guardar el usuario", e);
        }
    }

    @Override
    public void delete(User user) {
        String sql = "DELETE FROM `USER` WHERE USER_ID = ?";
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
        String sql = "SELECT * FROM `USER` WHERE USER_ID = ?";
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
    public List<User> getAll() {
        String sql = "SELECT * FROM `USER`";
        List<User> users = new ArrayList<>();

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
        String sql = "SELECT * FROM `USER` WHERE EMAIL = ?";
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