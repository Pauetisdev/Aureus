package cat.uvic.teknos.dam.aureus.repositories.jdbc;

import cat.uvic.teknos.dam.aureus.Coin;
import cat.uvic.teknos.dam.aureus.impl.CoinImpl;
import cat.uvic.teknos.dam.aureus.repositories.CoinRepository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.DataSource;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.CrudException;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.model.JdbcCoin;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JdbcCoinRepository implements CoinRepository {

    private final DataSource dataSource;

    public JdbcCoinRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(Coin coin) {
        String sql = "INSERT INTO COIN (COIN_NAME, COIN_YEAR, COIN_MATERIAL, COIN_WEIGHT, COIN_DIAMETER, ESTIMATED_VALUE, ORIGIN_COUNTRY, HISTORICAL_SIGNIFICANCE, COLLECTION_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, coin.getCoinName());
            ps.setInt(2, coin.getCoinYear());
            ps.setString(3, coin.getCoinMaterial());
            ps.setBigDecimal(4, coin.getCoinWeight());
            ps.setBigDecimal(5, coin.getCoinDiameter());
            ps.setBigDecimal(6, coin.getEstimatedValue());
            ps.setString(7, coin.getOriginCountry());
            ps.setString(8, coin.getHistoricalSignificance());

            // GET ESPECIFIC ID
            if (coin.getCollection() != null && coin.getCollection().getCollectionId() != null) {
                ps.setInt(9, coin.getCollection().getCollectionId());
            } else {
                ps.setNull(9, Types.INTEGER);
            }

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new CrudException("Error saving coin", e);
        }
    }


    @Override
    public void delete(Coin coin) {
        String sql = "DELETE FROM COIN WHERE COIN_ID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, coin.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new CrudException("Error deleting coin", e);
        }
    }

    @Override
    public Coin get(Integer id) {
        String sql = "SELECT * FROM COIN WHERE COIN_ID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Coin coin = new CoinImpl();
                    coin.setId(rs.getInt("COIN_ID"));
                    coin.setCoinName(rs.getString("COIN_NAME"));
                    coin.setCoinYear(rs.getInt("COIN_YEAR"));
                    coin.setCoinMaterial(rs.getString("COIN_MATERIAL"));
                    coin.setCoinWeight(rs.getBigDecimal("COIN_WEIGHT"));
                    coin.setCoinDiameter(rs.getBigDecimal("COIN_DIAMETER"));
                    coin.setEstimatedValue(rs.getBigDecimal("ESTIMATED_VALUE"));
                    coin.setOriginCountry(rs.getString("ORIGIN_COUNTRY"));
                    coin.setHistoricalSignificance(rs.getString("HISTORICAL_SIGNIFICANCE"));
                    // Aquí deberías setear la colección si quieres, por ejemplo:
                    // coin.setCollection(collectionObjectFromId(rs.getInt("COLLECTION_ID")));
                    return coin;
                }
                return null;
            }
        } catch (SQLException e) {
            throw new CrudException("Error getting coin", e);
        }
    }

    @Override
    public Set<Coin> getAll() {
        String sql = "SELECT * FROM COIN";
        Set<Coin> coins = new HashSet<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Coin coin = new CoinImpl();
                coin.setId(rs.getInt("COIN_ID"));
                coin.setCoinName(rs.getString("COIN_NAME"));
                coin.setCoinYear(rs.getInt("COIN_YEAR"));
                coin.setCoinMaterial(rs.getString("COIN_MATERIAL"));
                coin.setCoinWeight(rs.getBigDecimal("COIN_WEIGHT"));
                coin.setCoinDiameter(rs.getBigDecimal("COIN_DIAMETER"));
                coin.setEstimatedValue(rs.getBigDecimal("ESTIMATED_VALUE"));
                coin.setOriginCountry(rs.getString("ORIGIN_COUNTRY"));
                coin.setHistoricalSignificance(rs.getString("HISTORICAL_SIGNIFICANCE"));
                // Aquí también podrías cargar la colección si quieres
                coins.add(coin);
            }
            return coins;

        } catch (SQLException e) {
            throw new CrudException("Error getting all coins", e);
        }
    }

    @Override
    public List<Coin> findByMaterial(String material) {
        List<Coin> coins = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM COIN WHERE COIN_MATERIAL = ?")) {

            ps.setString(1, material);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JdbcCoin coin = new JdbcCoin();
                    coin.setId(rs.getInt("COIN_ID"));
                    coin.setCoinName(rs.getString("COIN_NAME"));
                    coin.setOriginCountry(rs.getString("ORIGIN_COUNTRY"));
                    coin.setCoinYear(rs.getInt("COIN_YEAR"));
                    coin.setCoinMaterial(rs.getString("COIN_MATERIAL"));
                    coin.setCoinWeight(rs.getBigDecimal("COIN_WEIGHT"));
                    coin.setCoinDiameter(rs.getBigDecimal("COIN_DIAMETER"));
                    coin.setEstimatedValue(rs.getBigDecimal("ESTIMATED_VALUE"));
                    coin.setHistoricalSignificance(rs.getString("HISTORICAL_SIGNIFICANCE"));
                    coins.add(coin);
                }
            }
        } catch (SQLException e) {
            throw new CrudException("Error finding coins by material", e);
        }
        return coins;
    }

    @Override
    public List<Coin> findByYear(Integer year) {
        List<Coin> coins = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM COIN WHERE COIN_YEAR = ?")) {

            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JdbcCoin coin = new JdbcCoin();
                    coin.setId(rs.getInt("COIN_ID"));
                    coin.setCoinName(rs.getString("COIN_NAME"));
                    coin.setOriginCountry(rs.getString("ORIGIN_COUNTRY"));
                    coin.setCoinYear(rs.getInt("COIN_YEAR"));
                    coin.setCoinMaterial(rs.getString("COIN_MATERIAL"));
                    coin.setCoinWeight(rs.getBigDecimal("COIN_WEIGHT"));
                    coin.setCoinDiameter(rs.getBigDecimal("COIN_DIAMETER"));
                    coin.setEstimatedValue(rs.getBigDecimal("ESTIMATED_VALUE"));
                    coin.setHistoricalSignificance(rs.getString("HISTORICAL_SIGNIFICANCE"));
                    coins.add(coin);
                }
            }
        } catch (SQLException e) {
            throw new CrudException("Error finding coins by year", e);
        }
        return coins;
    }
}
