
package cat.uvic.teknos.dam.aureus.repositories.jdbc;

import cat.uvic.teknos.dam.aureus.Collection;
import cat.uvic.teknos.dam.aureus.User;
import cat.uvic.teknos.dam.aureus.impl.CollectionImpl;
import cat.uvic.teknos.dam.aureus.repositories.CollectionRepository;
import cat.uvic.teknos.dam.aureus.repositories.UserRepository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.DataSource;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.*;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/**
 * JDBC implementation of CollectionRepository.
 * Handles database operations for collections, including user relationships.
 */
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
        // Verificamos los datos necesarios
        if (collection == null) {
            throw new InvalidDataException("Collection cannot be null");
        }

        if (collection.getUser() == null || collection.getUser().getId() == null) {
            throw new InvalidDataException("Collection must have an associated user");
        }

        // Verificamos que existe el usuario
        User user = userRepository.get(collection.getUser().getId());
        if (user == null) {
            throw new EntityNotFoundException("User not found with ID: " + collection.getUser().getId());
        }

        String sql;
        if (collection.getId() == null) {
            // Inserción de nueva colección
            sql = "INSERT INTO COLLECTION (NAME, DESCRIPTION, USER_ID) VALUES (?, ?, ?)";
        } else {
            // Actualización de colección existente
            sql = "UPDATE COLLECTION SET NAME = ?, DESCRIPTION = ?, USER_ID = ? WHERE COLLECTION_ID = ?";
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, collection.getCollectionName());
            ps.setString(2, collection.getDescription());
            ps.setInt(3, collection.getUser().getId());

            if (collection.getId() != null) {
                ps.setInt(4, collection.getId());
            }

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new RepositoryException("Failed to save collection");
            }

            // Obtenemos el ID generado para nuevas colecciones
            if (collection.getId() == null) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        collection.setId(rs.getInt(1));
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
                "JOIN USER u ON c.USER_ID = u.USER_ID " +
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
    public Set<Collection> getAll() {
        Set<Collection> collections = new HashSet<>();
        String sql = "SELECT c.*, u.USERNAME as USER_USERNAME " +
                "FROM COLLECTION c " +
                "JOIN USER u ON c.USER_ID = u.USER_ID";

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

    /**
     * Maps a ResultSet row to a Collection object.
     *
     * @param rs ResultSet containing collection data
     * @return Mapped Collection object
     * @throws SQLException if database access error occurs
     */
    private Collection mapCollection(ResultSet rs) throws SQLException {
        Collection collection = new CollectionImpl();
        collection.setId(rs.getInt("COLLECTION_ID"));
        collection.setCollectionName(rs.getString("NAME"));
        collection.setDescription(rs.getString("DESCRIPTION"));

        // Obtenemos el usuario completo usando el UserRepository
        User user = userRepository.get(rs.getInt("USER_ID"));
        if (user == null) {
            throw new EntityNotFoundException("User not found for collection: " + collection.getId());
        }
        collection.setUser(user);

        return collection;
    }
}