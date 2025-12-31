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

import java.sql.SQLException;
import java.util.*;

public class JdbcCoinCollectionRepository implements CoinCollectionRepository {

    private final DataSource dataSource;
    private final CoinRepository coinRepository;
    private final CollectionRepository collectionRepository;

    public JdbcCoinCollectionRepository(DataSource dataSource, CoinRepository coinRepository, CollectionRepository collectionRepository) {
        if (dataSource == null || coinRepository == null || collectionRepository == null) {
            throw new InvalidDataException("DataSource, CoinRepository, and CollectionRepository cannot be null");
        }
        this.dataSource = dataSource;
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
                     "INSERT INTO COIN_COLLECTION (COIN_ID, COLLECTION_ID) VALUES (?, ?)")) {
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
                     "DELETE FROM COIN_COLLECTION WHERE COIN_ID = ? AND COLLECTION_ID = ?")) {
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
    public List<CoinCollection> getAll() {
        List<CoinCollection> coinCollections = new ArrayList<>();
        List<int[]> ids = new ArrayList<>();
        try (var connection = dataSource.getConnection();
             var ps = connection.prepareStatement("SELECT * FROM COIN_COLLECTION");
             var rs = ps.executeQuery()) {
            while (rs.next()) {
                int coinIdDb = rs.getInt("COIN_ID");
                int collectionIdDb = rs.getInt("COLLECTION_ID");
                ids.add(new int[]{coinIdDb, collectionIdDb});
            }
        } catch (SQLException e) {
            throw new CrudException("Error retrieving all CoinCollections", e);
        }
        for (int[] pair : ids) {
            var coin = coinRepository.get(pair[0]);
            if (coin == null) throw new EntityNotFoundException("Coin not found with ID " + pair[0]);
            var collection = collectionRepository.get(pair[1]);
            if (collection == null) throw new EntityNotFoundException("Collection not found with ID " + pair[1]);
            var coinCollection = new CoinCollectionImpl();
            coinCollection.setCoin(coin);
            coinCollection.setCollection(collection);
            coinCollections.add(coinCollection);
        }
        return coinCollections;
    }


    @Override
    public List<CoinCollection> findByCollectionId(Integer collectionId) {
        if (collectionId == null) {
            throw new InvalidDataException("Collection ID cannot be null");
        }
        List<CoinCollection> coinCollections = new ArrayList<>();
        List<int[]> ids = new ArrayList<>();
        try (var connection = dataSource.getConnection();
             var ps = connection.prepareStatement("SELECT * FROM COIN_COLLECTION WHERE COLLECTION_ID = ?")) {
            ps.setInt(1, collectionId);
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    int coinIdDb = rs.getInt("COIN_ID");
                    int collectionIdDb = rs.getInt("COLLECTION_ID");
                    ids.add(new int[]{coinIdDb, collectionIdDb});
                }
            }
        } catch (SQLException e) {
            throw new CrudException("Error finding CoinCollections by Collection ID", e);
        }
        for (int[] pair : ids) {
            var coin = coinRepository.get(pair[0]);
            if (coin == null) throw new EntityNotFoundException("Coin not found with ID " + pair[0]);
            var collection = collectionRepository.get(pair[1]);
            if (collection == null) throw new EntityNotFoundException("Collection not found with ID " + pair[1]);
            var cc = new CoinCollectionImpl();
            cc.setCoin(coin);
            cc.setCollection(collection);
            coinCollections.add(cc);
        }
        return coinCollections;
    }

    @Override
    public List<CoinCollection> findByCoinId(Integer coinId) {
        if (coinId == null) {
            throw new InvalidDataException("Coin ID cannot be null");
        }
        List<CoinCollection> coinCollections = new ArrayList<>();
        List<int[]> ids = new ArrayList<>();
        try (var connection = dataSource.getConnection();
             var ps = connection.prepareStatement("SELECT * FROM COIN_COLLECTION WHERE COIN_ID = ?")) {
            ps.setInt(1, coinId);
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    int coinIdDb = rs.getInt("COIN_ID");
                    int collectionIdDb = rs.getInt("COLLECTION_ID");
                    ids.add(new int[]{coinIdDb, collectionIdDb});
                }
            }
        } catch (SQLException e) {
            throw new CrudException("Error finding CoinCollections by Coin ID", e);
        }
        for (int[] pair : ids) {
            var coin = coinRepository.get(pair[0]);
            if (coin == null) throw new EntityNotFoundException("Coin not found with ID " + pair[0]);
            var collection = collectionRepository.get(pair[1]);
            if (collection == null) throw new EntityNotFoundException("Collection not found with ID " + pair[1]);
            var cc = new CoinCollectionImpl();
            cc.setCoin(coin);
            cc.setCollection(collection);
            coinCollections.add(cc);
        }
        return coinCollections;
    }
}