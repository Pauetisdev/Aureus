package cat.uvic.teknos.dam.aureus.repositories.jdbc;

import cat.uvic.teknos.dam.aureus.Collection;
import cat.uvic.teknos.dam.aureus.impl.CollectionImpl;
import cat.uvic.teknos.dam.aureus.repositories.CollectionRepository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.DataSource;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.CrudException;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JdbcCollectionRepository implements CollectionRepository {

    private final DataSource dataSource;

    public JdbcCollectionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(Collection collection) {
        String sql = "INSERT INTO COLLECTION (COLLECTION_NAME, DESCRIPTION, USER_ID) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, collection.getCollectionName());
            ps.setString(2, collection.getDescription());
            ps.setInt(3, collection.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new CrudException("Error guardando colección", e);
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
                }
                return null;
            }

        } catch (SQLException e) {
            throw new CrudException("Error obteniendo colección", e);
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
                c.setId(rs.getInt("USER_ID"));
                collections.add(c);
            }
            return collections;

        } catch (SQLException e) {
            throw new CrudException("Error obteniendo todas las colecciones", e);
        }
    }

}
