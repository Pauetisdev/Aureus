package cat.uvic.teknos.dam.aureus.repositories.jdbc;

import cat.uvic.teknos.dam.aureus.CoinCollection;
import cat.uvic.teknos.dam.aureus.impl.CoinCollectionImpl;
import cat.uvic.teknos.dam.aureus.repositories.CoinCollectionRepository;
import cat.uvic.teknos.dam.aureus.repositories.CoinRepository;
import cat.uvic.teknos.dam.aureus.repositories.CollectionRepository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.DataSource;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.CrudException;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.EntityNotFoundException;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.InvalidDataException;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.model.JdbcCoinCollection;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

public class JdbcCoinCollectionRepository implements CoinCollectionRepository {

    private final DataSource dataSource;
    private CoinRepository coinRepository = null;
    private CollectionRepository collectionRepository = null;

    // Constructor corregido para recibir las dependencias
    public JdbcCoinCollectionRepository(DataSource dataSource) {
        throw new InvalidDataException("DataSource, CoinRepository, and CollectionRepository cannot be null");
    }

    public JdbcCoinCollectionRepository(DataSource dataSource, DataSource dataSource1, CoinRepository coinRepository, CollectionRepository collectionRepository) {
        this.dataSource = dataSource1;
        this.coinRepository = coinRepository;
        this.collectionRepository = collectionRepository;
    }

    @Override
    public void save(CoinCollection value) {
        if (value == null || value.getCoin() == null || value.getCollection() == null) {
            throw new InvalidDataException("CoinCollection or its components cannot be null");
        }

        try (var connection = dataSource.getConnection();
             var ps = connection.prepareStatement(
                     "INSERT INTO COINS_COLLECTION (COIN_ID, COLLECTION_ID) VALUES (?, ?)")) {
            ps.setInt(1, value.getCoin().getId());
            ps.setInt(2, value.getCollection().getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new CrudException("Error saving CoinCollection", e);
        }
    }

    @Override
    public void delete(CoinCollection value) {
        if (value == null || value.getCoin() == null || value.getCollection() == null) {
            throw new InvalidDataException("CoinCollection or its components cannot be null");
        }

        try (var connection = dataSource.getConnection();
             var ps = connection.prepareStatement(
                     "DELETE FROM COINS_COLLECTION WHERE COIN_ID = ? AND COLLECTION_ID = ?")) {
            ps.setInt(1, value.getCoin().getId());
            ps.setInt(2, value.getCollection().getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new CrudException("Error deleting CoinCollection", e);
        }
    }

    @Override
    public CoinCollection get(Integer id) {
        throw new UnsupportedOperationException(
                "CoinCollection has a composite primary key and cannot be retrieved with just one ID.");
    }

    @Override
    public Set<CoinCollection> getAll() {
        var coinCollections = new HashSet<CoinCollection>();

        try (var connection = dataSource.getConnection();
             var ps = connection.prepareStatement("SELECT * FROM COINS_COLLECTION")) {

            var rs = ps.executeQuery();

            while (rs.next()) {
                var coin = coinRepository.get(rs.getInt("COIN_ID"));
                if (coin == null) throw new EntityNotFoundException("Coin not found with ID " + rs.getInt("COIN_ID"));
                var collection = collectionRepository.get(rs.getInt("COLLECTION_ID"));
                if (collection == null) throw new EntityNotFoundException("Collection not found with ID " + rs.getInt("COLLECTION_ID"));

                var coinCollection = new CoinCollectionImpl();
                coinCollection.setCoin(coin);
                coinCollection.setCollection(collection);

                coinCollections.add(coinCollection);
            }
        } catch (SQLException e) {
            throw new CrudException("Error retrieving all CoinCollections", e);
        }

        return coinCollections;
    }

    @Override
    public List<CoinCollection> findByCollectionId(Integer collectionId) {
        if (collectionId == null) {
            throw new InvalidDataException("Collection ID cannot be null");
        }
        List<CoinCollection> coinCollections = new ArrayList<>();
        try (var connection = dataSource.getConnection();
             var ps = connection.prepareStatement("SELECT * FROM COINS_COLLECTION WHERE COLLECTION_ID = ?")) {
            ps.setInt(1, collectionId);
            var rs = ps.executeQuery();
            while (rs.next()) {
                JdbcCoinCollection cc = new JdbcCoinCollection();
                var coin = coinRepository.get(rs.getInt("COIN_ID"));
                if (coin == null) throw new EntityNotFoundException("Coin not found with ID " + rs.getInt("COIN_ID"));
                var collection = collectionRepository.get(rs.getInt("COLLECTION_ID"));
                if (collection == null) throw new EntityNotFoundException("Collection not found with ID " + rs.getInt("COLLECTION_ID"));
                cc.setCoin(coin);
                cc.setCollection(collection);
                coinCollections.add(cc);
            }
        } catch (SQLException e) {
            throw new CrudException("Error finding CoinCollections by Collection ID", e);
        }
        return coinCollections;
    }

    @Override
    public List<CoinCollection> findByCoinId(Integer coinId) {
        if (coinId == null) {
            throw new InvalidDataException("Coin ID cannot be null");
        }
        List<CoinCollection> coinCollections = new ArrayList<>();
        try (var connection = dataSource.getConnection();
             var ps = connection.prepareStatement("SELECT * FROM COINS_COLLECTION WHERE COIN_ID = ?")) {
            ps.setInt(1, coinId);
            var rs = ps.executeQuery();
            while (rs.next()) {
                JdbcCoinCollection cc = new JdbcCoinCollection();
                var coin = coinRepository.get(rs.getInt("COIN_ID"));
                if (coin == null) throw new EntityNotFoundException("Coin not found with ID " + rs.getInt("COIN_ID"));
                var collection = collectionRepository.get(rs.getInt("COLLECTION_ID"));
                if (collection == null) throw new EntityNotFoundException("Collection not found with ID " + rs.getInt("COLLECTION_ID"));
                cc.setCoin(coin);
                cc.setCollection(collection);
                coinCollections.add(cc);
            }
        } catch (SQLException e) {
            throw new CrudException("Error finding CoinCollections by Coin ID", e);
        }
        return coinCollections;
    }
}
