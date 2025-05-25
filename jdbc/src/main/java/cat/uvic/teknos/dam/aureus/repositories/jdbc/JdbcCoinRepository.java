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

            if (coin.getId() != null) {
                ps.setInt(9, coin.getId());
            } else {
                ps.setNull(9, Types.INTEGER);
            }

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new CrudException("Error guardando moneda", e);
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
            throw new CrudException("Error borrando moneda", e);
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
                    coin.setId(rs.getInt("COLLECTION_ID"));
                    return coin;
                }
                return null;
            }
        } catch (SQLException e) {
            throw new CrudException("Error obteniendo moneda", e);
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
                coin.setId(rs.getInt("COLLECTION_ID"));
                coins.add(coin);
            }
            return coins;

        } catch (SQLException e) {
            throw new CrudException("Error obteniendo todas las monedas", e);
        }
    }


    @Override
    public List<Coin> findByMaterial(String material) {
        List<Coin> coins = new ArrayList<>();
        var connection = dataSource.getConnection();
        try (var ps = connection.prepareStatement("SELECT * FROM COIN WHERE coin_material = ?")) {
            ps.setString(1, material);
            var rs = ps.executeQuery();
            while (rs.next()) {
                JdbcCoin coin = new JdbcCoin();
                coin.setId(rs.getInt("id"));
                coin.setCoinName(rs.getString("coin_name"));
                coin.setOriginCountry(rs.getString("origin_country"));
                coin.setCoinYear(rs.getInt("coin_year"));
                coin.setCoinMaterial(rs.getString("coin_material"));
                coin.setCoinWeight(rs.getBigDecimal("coin_weight"));
                coin.setCoinDiameter(rs.getBigDecimal("coin_diameter"));
                coin.setEstimatedValue(rs.getBigDecimal("estimated_value"));
                coin.setHistoricalSignificance(rs.getString("historical_significance"));
                coins.add(coin);
            }
        } catch (SQLException e) {
            throw new CrudException(e);
        }
        return coins;
    }

    @Override
    public List<Coin> findByYear(Integer year) {
        var coins = new ArrayList<Coin>();
        var connection = dataSource.getConnection();
        try (var ps = connection.prepareStatement("SELECT * FROM COIN WHERE coin_year = ?")) {
            ps.setInt(1, year);
            var rs = ps.executeQuery();
            while (rs.next()) {
                JdbcCoin coin = new JdbcCoin();
                coin.setId(rs.getInt("id"));
                coin.setCoinName(rs.getString("coin_name"));
                coin.setOriginCountry(rs.getString("origin_country"));
                coin.setCoinYear(rs.getInt("coin_year"));
                coin.setCoinMaterial(rs.getString("coin_material"));
                coin.setCoinWeight(rs.getBigDecimal("coin_weight"));
                coin.setCoinDiameter(rs.getBigDecimal("coin_diameter"));
                coin.setEstimatedValue(rs.getBigDecimal("estimated_value"));
                coin.setHistoricalSignificance(rs.getString("historical_significance"));
                coins.add(coin);
            }
        } catch (SQLException e) {
            throw new CrudException(e);
        }
        return coins;
    }
}
