package cat.uvic.teknos.dam.aureus.repositories.jdbc;

import cat.uvic.teknos.dam.aureus.Collection;
import cat.uvic.teknos.dam.aureus.impl.CollectionImpl;
import cat.uvic.teknos.dam.aureus.repositories.CollectionRepository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.DataSource;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.CrudException;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.EntityNotFoundException;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.InvalidDataException;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.RepositoryException;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class JdbcCollectionRepository implements CollectionRepository {

    private final DataSource dataSource;

    public JdbcCollectionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(Collection collection) {
        if (collection.getCollectionName() == null || collection.getCollectionName().isBlank()) {
            throw new InvalidDataException("Collection name cannot be empty");
        }
        // add other validations here...

        String sql = "INSERT INTO COLLECTION (COLLECTION_NAME, DESCRIPTION, USER_ID) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, collection.getCollectionName());
            ps.setString(2, collection.getDescription());
            ps.setInt(3, collection.getId());

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    collection.setId(generatedKeys.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RepositoryException("Error saving collection", e);
        }
    }


    @Override
    public void delete(Collection collection) {
        String sql = "DELETE FROM COLLECTION WHERE COLLECTION_ID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, collection.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new CrudException("Error borrando colección", e);
        }
    }

    @Override
    public Collection get(Integer id) {
        String sql = "SELECT * FROM COLLECTION WHERE COLLECTION_ID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Collection c = new CollectionImpl();
                    c.setId(rs.getInt("COLLECTION_ID"));
                    c.setCollectionName(rs.getString("COLLECTION_NAME"));
                    c.setDescription(rs.getString("DESCRIPTION"));
                    c.setId(rs.getInt("USER_ID"));
                    return c;
                } else {
                    throw new EntityNotFoundException("Collection with ID " + id + " not found.");
                }
            }

        } catch (SQLException e) {
            throw new RepositoryException("Database access error", e);
        }
    }


    @Override
    public Set<Collection> getAll() {
        String sql = "SELECT * FROM COLLECTION";
        Set<Collection> collections = new HashSet<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Collection c = new CollectionImpl();
                c.setId(rs.getInt("COLLECTION_ID"));
                c.setCollectionName(rs.getString("COLLECTION_NAME"));
                c.setDescription(rs.getString("DESCRIPTION"));
                c.setId(rs.getInt("USER_ID"));  // ID usuario bien puesto
                collections.add(c);
            }
            return collections;

        } catch (SQLException e) {
            throw new CrudException("Error obteniendo todas las colecciones", e);
        }
    }
}
