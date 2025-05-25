package cat.uvic.teknos.dam.aureus.repositories.jdbc.model;

import cat.uvic.teknos.dam.aureus.Coin;
import cat.uvic.teknos.dam.aureus.CoinCollection;

import java.math.BigDecimal;

public class JdbcCoin implements Coin {
    private Integer id;
    private String coinName;
    private String originCountry;
    private Integer coinYear;
    private String coinMaterial;
    private BigDecimal coinWeight;
    private BigDecimal coinDiameter;
    private BigDecimal estimatedValue;
    private String historicalSignificance;
    private CoinCollection collection;

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public String getCoinName() {
        return coinName;
    }

    @Override
    public Integer getCoinYear() {
        return coinYear;
    }

    @Override
    public String getCoinMaterial() {
        return coinMaterial;
    }

    @Override
    public BigDecimal getCoinWeight() {
        return coinWeight;
    }

    @Override
    public BigDecimal getCoinDiameter() {
        return coinDiameter;
    }

    @Override
    public BigDecimal getEstimatedValue() {
        return estimatedValue;
    }

    @Override
    public String getOriginCountry() {
        return originCountry;
    }

    @Override
    public String getHistoricalSignificance() {
        return historicalSignificance;
    }

    @Override
    public CoinCollection getCollection() {
        return collection;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public void setCoinName(String coinName) {
        this.coinName = coinName;
    }

    @Override
    public void setCoinYear(Integer coinYear) {
        this.coinYear = coinYear;
    }

    @Override
    public void setCoinMaterial(String coinMaterial) {
        this.coinMaterial = coinMaterial;
    }

    @Override
    public void setCoinWeight(BigDecimal coinWeight) {
        this.coinWeight = coinWeight;
    }

    @Override
    public void setCoinDiameter(BigDecimal coinDiameter) {
        this.coinDiameter = coinDiameter;
    }

    @Override
    public void setEstimatedValue(BigDecimal estimatedValue) {
        this.estimatedValue = estimatedValue;
    }

    @Override
    public void setOriginCountry(String originCountry) {
        this.originCountry = originCountry;
    }

    @Override
    public void setHistoricalSignificance(String historicalSignificance) {
        this.historicalSignificance = historicalSignificance;
    }

    @Override
    public void setCollection(CoinCollection collection) {
        this.collection = collection;
    }
}
