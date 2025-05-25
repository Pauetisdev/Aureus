package cat.uvic.teknos.dam.aureus.repositories.jdbc;

import cat.uvic.teknos.dam.aureus.CoinCollection;
import cat.uvic.teknos.dam.aureus.impl.CoinCollectionImpl;
import cat.uvic.teknos.dam.aureus.repositories.CoinCollectionRepository;
import cat.uvic.teknos.dam.aureus.repositories.CoinRepository;
import cat.uvic.teknos.dam.aureus.repositories.CollectionRepository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.DataSource;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.CrudException;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.model.JdbcCoinCollection;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

public class JdbcCoinCollectionRepository implements CoinCollectionRepository {

    private final DataSource dataSource;
    private final CoinRepository coinRepository;
    private final CollectionRepository collectionRepository;

    public JdbcCoinCollectionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        this.coinRepository = coinRepository;
        this.collectionRepository = collectionRepository;
    }

    @Override
    public void save(CoinCollection value) {
        var connection = dataSource.getConnection();
        try (var ps = connection.prepareStatement(
                "INSERT INTO COINS_COLLECTION (COIN_ID, COLLECTION_ID) VALUES (?, ?)")) {
            ps.setInt(1, value.getCoin().getId());
            ps.setInt(2, value.getCollection().getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new CrudException(e);
        }
    }

    @Override
    public void delete(CoinCollection value) {
        var connection = dataSource.getConnection();
        try (var ps = connection.prepareStatement(
                "DELETE FROM COINS_COLLECTION WHERE COIN_ID = ? AND COLLECTION_ID = ?")) {
            ps.setInt(1, value.getCoin().getId());
            ps.setInt(2, value.getCollection().getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new CrudException(e);
        }
    }

    @Override
    public CoinCollection get(Integer id) {
        throw new UnsupportedOperationException(
                "CoinCollection tiene clave primaria compuesta, no se puede obtener solo con un ID.");
    }

    @Override
    public Set<CoinCollection> getAll() {
        var connection = dataSource.getConnection();
        var coinCollections = new HashSet<CoinCollection>();

        try (var ps = connection.prepareStatement("SELECT * FROM COINS_COLLECTION")) {
            var rs = ps.executeQuery();

            while (rs.next()) {
                var coin = coinRepository.get(rs.getInt("COIN_ID"));
                var collection = collectionRepository.get(rs.getInt("COLLECTION_ID"));

                var coinCollection = new CoinCollectionImpl();
                coinCollection.setCoin(coin);
                coinCollection.setCollection(collection);

                coinCollections.add(coinCollection);
            }
        } catch (SQLException e) {
            throw new CrudException(e);
        }

        return coinCollections;
    }

    @Override
    public List<CoinCollection> findByCollectionId(Integer collectionId) {
        List<CoinCollection> coinCollections = new ArrayList<>();
        var connection = dataSource.getConnection();
        try (var ps = connection.prepareStatement("SELECT * FROM COINS_COLLECTION WHERE COLLECTION_ID = ?")) {
            ps.setInt(1, collectionId);
            var rs = ps.executeQuery();
            while (rs.next()) {
                JdbcCoinCollection cc = new JdbcCoinCollection();
                var coin = coinRepository.get(rs.getInt("COIN_ID"));
                var collection = collectionRepository.get(rs.getInt("COLLECTION_ID"));
                cc.setCoin(coin);
                cc.setCollection(collection);
                coinCollections.add(cc);
            }
        } catch (SQLException e) {
            throw new CrudException(e);
        }
        return coinCollections;
    }

    @Override
    public List<CoinCollection> findByCoinId(Integer coinId) {
        List<CoinCollection> coinCollections = new ArrayList<>();
        var connection = dataSource.getConnection();
        try (var ps = connection.prepareStatement("SELECT * FROM COINS_COLLECTION WHERE COIN_ID = ?")) {
            ps.setInt(1, coinId);
            var rs = ps.executeQuery();
            while (rs.next()) {
                JdbcCoinCollection cc = new JdbcCoinCollection();
                var coin = coinRepository.get(rs.getInt("COIN_ID"));
                var collection = collectionRepository.get(rs.getInt("COLLECTION_ID"));
                cc.setCoin(coin);
                cc.setCollection(collection);
                coinCollections.add(cc);
            }
        } catch (SQLException e) {
            throw new CrudException(e);
        }
        return coinCollections;
    }

}
