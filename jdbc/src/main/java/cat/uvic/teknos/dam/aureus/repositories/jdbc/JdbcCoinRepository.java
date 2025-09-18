package cat.uvic.teknos.dam.aureus.repositories.jdbc;

import cat.uvic.teknos.dam.aureus.Coin;
import cat.uvic.teknos.dam.aureus.CoinCollection;
import cat.uvic.teknos.dam.aureus.repositories.CoinRepository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.DataSource;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.RepositoryException;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

public class JdbcCoinRepository implements CoinRepository {

    private final DataSource dataSource;

    public JdbcCoinRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(Coin coin) {
        if (coin == null) throw new IllegalArgumentException("Coin cannot be null");

        Integer collectionId = coin.getCollection() != null ? coin.getCollection().getId() : null;

        if (coin.getId() == null) {
            String sql = "INSERT INTO COIN (COIN_NAME, COIN_YEAR, COIN_MATERIAL, COIN_WEIGHT, COIN_DIAMETER, ESTIMATED_VALUE, ORIGIN_COUNTRY, HISTORICAL_SIGNIFICANCE, COLLECTION_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (var conn = dataSource.getConnection();
                 var ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, coin.getCoinName());
                ps.setInt(2, coin.getCoinYear());
                ps.setString(3, coin.getCoinMaterial());
                ps.setBigDecimal(4, coin.getCoinWeight());
                ps.setBigDecimal(5, coin.getCoinDiameter());
                ps.setBigDecimal(6, coin.getEstimatedValue());
                ps.setString(7, coin.getOriginCountry());
                ps.setString(8, coin.getHistoricalSignificance());
                if (collectionId != null) {
                    ps.setInt(9, collectionId);
                } else {
                    ps.setNull(9, java.sql.Types.INTEGER);
                }
                ps.executeUpdate();
                try (var rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        coin.setId(rs.getInt(1));
                    }
                }
            } catch (SQLException e) {
                throw new RepositoryException("Error saving coin", e);
            }
        } else {
            String sql = "UPDATE COIN SET COIN_NAME=?, COIN_YEAR=?, COIN_MATERIAL=?, COIN_WEIGHT=?, COIN_DIAMETER=?, ESTIMATED_VALUE=?, ORIGIN_COUNTRY=?, HISTORICAL_SIGNIFICANCE=?, COLLECTION_ID=? WHERE COIN_ID=?";
            try (var conn = dataSource.getConnection();
                 var ps = conn.prepareStatement(sql)) {
                ps.setString(1, coin.getCoinName());
                ps.setInt(2, coin.getCoinYear());
                ps.setString(3, coin.getCoinMaterial());
                ps.setBigDecimal(4, coin.getCoinWeight());
                ps.setBigDecimal(5, coin.getCoinDiameter());
                ps.setBigDecimal(6, coin.getEstimatedValue());
                ps.setString(7, coin.getOriginCountry());
                ps.setString(8, coin.getHistoricalSignificance());
                if (collectionId != null) {
                    ps.setInt(9, collectionId);
                } else {
                    ps.setNull(9, java.sql.Types.INTEGER);
                }
                ps.setInt(10, coin.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RepositoryException("Error updating coin", e);
            }
        }
    }

    @Override
    public void delete(Coin coin) {
        if (coin == null || coin.getId() == null) {
            throw new IllegalArgumentException("Coin or Coin ID cannot be null");
        }
        try (var connection = dataSource.getConnection();
             var ps = connection.prepareStatement("DELETE FROM COIN WHERE COIN_ID = ?")) {
            ps.setInt(1, coin.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Error deleting Coin", e);
        }
    }

    @Override
    public Coin get(Integer id) {
        if (id == null) {
            return null;
        }
        try (var connection = dataSource.getConnection();
             var ps = connection.prepareStatement("SELECT * FROM COIN WHERE COIN_ID = ?")) {
            ps.setInt(1, id);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    var coin = new cat.uvic.teknos.dam.aureus.impl.CoinImpl();
                    coin.setId(rs.getInt("COIN_ID"));
                    coin.setCoinName(rs.getString("COIN_NAME"));
                    coin.setOriginCountry(rs.getString("ORIGIN_COUNTRY"));
                    coin.setCoinYear(rs.getInt("COIN_YEAR"));
                    coin.setCoinMaterial(rs.getString("COIN_MATERIAL"));
                    coin.setCoinWeight(rs.getBigDecimal("COIN_WEIGHT"));
                    coin.setCoinDiameter(rs.getBigDecimal("COIN_DIAMETER"));
                    coin.setEstimatedValue(rs.getBigDecimal("ESTIMATED_VALUE"));
                    coin.setHistoricalSignificance(rs.getString("HISTORICAL_SIGNIFICANCE"));
                    Integer collectionId = (Integer) rs.getObject("COLLECTION_ID");
                    if (collectionId != null) {
                        CoinCollection collection = new cat.uvic.teknos.dam.aureus.impl.CoinCollectionImpl();
                        collection.setId(collectionId);
                        coin.setCollection(collection);
                    }
                    return coin;
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Error getting Coin", e);
        }
        return null;
    }

    @Override
    public List<Coin> getAll() {
        List<Coin> coins = new ArrayList<>();
        try (var connection = dataSource.getConnection();
             var ps = connection.prepareStatement("SELECT * FROM COIN");
             var rs = ps.executeQuery()) {
            while (rs.next()) {
                var coin = new cat.uvic.teknos.dam.aureus.impl.CoinImpl();
                coin.setId(rs.getInt("COIN_ID"));
                coin.setCoinName(rs.getString("COIN_NAME"));
                coin.setOriginCountry(rs.getString("ORIGIN_COUNTRY"));
                coin.setCoinYear(rs.getInt("COIN_YEAR"));
                coin.setCoinMaterial(rs.getString("COIN_MATERIAL"));
                coin.setCoinWeight(rs.getBigDecimal("COIN_WEIGHT"));
                coin.setCoinDiameter(rs.getBigDecimal("COIN_DIAMETER"));
                coin.setEstimatedValue(rs.getBigDecimal("ESTIMATED_VALUE"));
                coin.setHistoricalSignificance(rs.getString("HISTORICAL_SIGNIFICANCE"));
                Integer collectionId = (Integer) rs.getObject("COLLECTION_ID");
                if (collectionId != null) {
                    CoinCollection collection = new cat.uvic.teknos.dam.aureus.impl.CoinCollectionImpl();
                    collection.setId(collectionId);
                    coin.setCollection(collection);
                }
                coins.add(coin);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Error getting all Coins", e);
        }
        return coins;
    }


    @Override
    public List<Coin> findByMaterial(String material) {
        List<Coin> coins = new ArrayList<>();
        try (var connection = dataSource.getConnection();
             var ps = connection.prepareStatement("SELECT * FROM COIN WHERE COIN_MATERIAL = ?")) {
            ps.setString(1, material);
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    var coin = new cat.uvic.teknos.dam.aureus.impl.CoinImpl();
                    coin.setId(rs.getInt("COIN_ID"));
                    coin.setCoinName(rs.getString("COIN_NAME"));
                    coin.setOriginCountry(rs.getString("ORIGIN_COUNTRY"));
                    coin.setCoinYear(rs.getInt("COIN_YEAR"));
                    coin.setCoinMaterial(rs.getString("COIN_MATERIAL"));
                    coin.setCoinWeight(rs.getBigDecimal("COIN_WEIGHT"));
                    coin.setCoinDiameter(rs.getBigDecimal("COIN_DIAMETER"));
                    coin.setEstimatedValue(rs.getBigDecimal("ESTIMATED_VALUE"));
                    coin.setHistoricalSignificance(rs.getString("HISTORICAL_SIGNIFICANCE"));
                    Integer collectionId = (Integer) rs.getObject("COLLECTION_ID");
                    if (collectionId != null) {
                        CoinCollection collection = new cat.uvic.teknos.dam.aureus.impl.CoinCollectionImpl();
                        collection.setId(collectionId);
                        coin.setCollection(collection);
                    }
                    coins.add(coin);
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Error finding Coins by material", e);
        }
        return coins;
    }

    @Override
    public List<Coin> findByYear(Integer year) {
        List<Coin> coins = new ArrayList<>();
        try (var connection = dataSource.getConnection();
             var ps = connection.prepareStatement("SELECT * FROM COIN WHERE COIN_YEAR = ?")) {
            ps.setInt(1, year);
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    var coin = new cat.uvic.teknos.dam.aureus.impl.CoinImpl();
                    coin.setId(rs.getInt("COIN_ID"));
                    coin.setCoinName(rs.getString("COIN_NAME"));
                    coin.setOriginCountry(rs.getString("ORIGIN_COUNTRY"));
                    coin.setCoinYear(rs.getInt("COIN_YEAR"));
                    coin.setCoinMaterial(rs.getString("COIN_MATERIAL"));
                    coin.setCoinWeight(rs.getBigDecimal("COIN_WEIGHT"));
                    coin.setCoinDiameter(rs.getBigDecimal("COIN_DIAMETER"));
                    coin.setEstimatedValue(rs.getBigDecimal("ESTIMATED_VALUE"));
                    coin.setHistoricalSignificance(rs.getString("HISTORICAL_SIGNIFICANCE"));
                    Integer collectionId = (Integer) rs.getObject("COLLECTION_ID");
                    if (collectionId != null) {
                        CoinCollection collection = new cat.uvic.teknos.dam.aureus.impl.CoinCollectionImpl();
                        collection.setId(collectionId);
                        coin.setCollection(collection);
                    }
                    coins.add(coin);
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Error finding Coins by year", e);
        }
        return coins;
    }
}