package cat.uvic.teknos.dam.aureus.repositories.jdbc;

import cat.uvic.teknos.dam.aureus.Collection;
import cat.uvic.teknos.dam.aureus.User;
import cat.uvic.teknos.dam.aureus.impl.CollectionImpl;
import cat.uvic.teknos.dam.aureus.impl.UserImpl;
import cat.uvic.teknos.dam.aureus.repositories.CollectionRepository;
import cat.uvic.teknos.dam.aureus.repositories.UserRepository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.DataSource;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JdbcCollectionRepository implements CollectionRepository {

    private final DataSource dataSource;
    private final UserRepository userRepository;

    public JdbcCollectionRepository(DataSource dataSource, UserRepository userRepository) {
        if (dataSource == null || userRepository == null) {
            throw new InvalidDataException("DataSource and UserRepository cannot be null");
        }
        this.dataSource = dataSource;
        this.userRepository = userRepository;
    }

    @Override
    public void save(Collection collection) {
        if (collection == null) {
            throw new InvalidDataException("Collection cannot be null");
        }

        if (collection.getUser() == null || collection.getUser().getId() == null) {
            throw new InvalidDataException("Collection must have an associated user");
        }

        User user = userRepository.get(collection.getUser().getId());
        if (user == null) {
            throw new EntityNotFoundException("User not found with ID: " + collection.getUser().getId());
        }

        String sql;
        boolean isInsert = (collection.getId() == null);

        if (isInsert) {
            sql = "INSERT INTO COLLECTION (COLLECTION_NAME, DESCRIPTION, USER_ID) VALUES (?, ?, ?)";
        } else {
            sql = "UPDATE COLLECTION SET COLLECTION_NAME = ?, DESCRIPTION = ?, USER_ID = ? WHERE COLLECTION_ID = ?";
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql,
                     isInsert ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS)) {

            ps.setString(1, collection.getCollectionName());
            ps.setString(2, collection.getDescription());
            ps.setInt(3, collection.getUser().getId());

            if (!isInsert) {
                ps.setInt(4, collection.getId());
            }

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new RepositoryException("Failed to save collection");
            }

            if (isInsert) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        collection.setId(rs.getInt(1)); // 🔥 ahora sí tienes el ID
                    }
                }
            }

        } catch (SQLException e) {
            throw new RepositoryException("Error saving collection", e);
        }
    }

    @Override
    public void delete(Collection collection) {
        if (collection == null || collection.getId() == null) {
            throw new InvalidDataException("Collection or Collection ID cannot be null");
        }

        String sql = "DELETE FROM COLLECTION WHERE COLLECTION_ID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, collection.getId());
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new EntityNotFoundException("Collection not found with ID: " + collection.getId());
            }

        } catch (SQLException e) {
            throw new RepositoryException("Error deleting collection", e);
        }
    }

    @Override
    public Collection get(Integer id) {
        if (id == null) {
            throw new InvalidDataException("Collection ID cannot be null");
        }

        String sql = "SELECT c.*, u.USERNAME as USER_USERNAME " +
                "FROM COLLECTION c " +
                "JOIN `USER` u ON c.USER_ID = u.USER_ID " +
                "WHERE c.COLLECTION_ID = ?";


        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapCollection(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            throw new RepositoryException("Error getting collection", e);
        }
    }

    @Override
    public List<Collection> getAll() {
        List<Collection> collections = new ArrayList<>();
        String sql = "SELECT c.*, u.USERNAME as USER_USERNAME " +
                "FROM COLLECTION c " +
                "JOIN `USER` u ON c.USER_ID = u.USER_ID";



        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                collections.add(mapCollection(rs));
            }

            return collections;

        } catch (SQLException e) {
            throw new RepositoryException("Error getting all collections", e);
        }
    }

    private Collection mapCollection(ResultSet rs) throws SQLException {
        Collection collection = new CollectionImpl();
        collection.setId(rs.getInt("COLLECTION_ID"));
        collection.setCollectionName(rs.getString("COLLECTION_NAME"));
        collection.setDescription(rs.getString("DESCRIPTION"));

        User user = new UserImpl();
        user.setId(rs.getInt("USER_ID"));
        user.setUsername(rs.getString("USER_USERNAME"));
        // Si necesitas más campos, añádelos aquí usando rs.getXXX("NOMBRE_COLUMNA")
        collection.setUser(user);

        return collection;
    }
}