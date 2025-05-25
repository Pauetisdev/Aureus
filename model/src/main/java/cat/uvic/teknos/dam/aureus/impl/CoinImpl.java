package cat.uvic.teknos.dam.aureus.impl;

import cat.uvic.teknos.dam.aureus.Coin;
import cat.uvic.teknos.dam.aureus.CoinCollection;
import java.math.BigDecimal;

public class CoinImpl implements Coin {

    private Integer id;
    private String coinName;
    private Integer coinYear;
    private String coinMaterial;
    private BigDecimal coinWeight;
    private BigDecimal coinDiameter;
    private BigDecimal estimatedValue;
    private String originCountry;
    private String historicalSignificance;
    private CoinCollection collection;

    public CoinImpl() {}

    public CoinImpl(Integer id, String coinName, Integer coinYear, String coinMaterial,
                    BigDecimal coinWeight, BigDecimal coinDiameter, BigDecimal estimatedValue,
                    String originCountry, String historicalSignificance, CoinCollection collection) {
        setId(id);
        setCoinName(coinName);
        setCoinYear(coinYear);
        setCoinMaterial(coinMaterial);
        setCoinWeight(coinWeight);
        setCoinDiameter(coinDiameter);
        setEstimatedValue(estimatedValue);
        setOriginCountry(originCountry);
        setHistoricalSignificance(historicalSignificance);
        setCollection(collection);
    }

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String getCoinName() {
        return coinName;
    }

    public void setCoinName(String coinName) {
        this.coinName = coinName;
    }

    @Override
    public Integer getCoinYear() {
        return coinYear;
    }

    public void setCoinYear(Integer coinYear) {
        this.coinYear = coinYear;
    }

    @Override
    public String getCoinMaterial() {
        return coinMaterial;
    }

    public void setCoinMaterial(String coinMaterial) {
        this.coinMaterial = coinMaterial;
    }

    @Override
    public BigDecimal getCoinWeight() {
        return coinWeight;
    }

    public void setCoinWeight(BigDecimal coinWeight) {
        this.coinWeight = coinWeight;
    }

    @Override
    public BigDecimal getCoinDiameter() {
        return coinDiameter;
    }

    public void setCoinDiameter(BigDecimal coinDiameter) {
        this.coinDiameter = coinDiameter;
    }

    @Override
    public BigDecimal getEstimatedValue() {
        return estimatedValue;
    }

    public void setEstimatedValue(BigDecimal estimatedValue) {
        this.estimatedValue = estimatedValue;
    }

    @Override
    public String getOriginCountry() {
        return originCountry;
    }

    public void setOriginCountry(String originCountry) {
        this.originCountry = originCountry;
    }

    @Override
    public String getHistoricalSignificance() {
        return historicalSignificance;
    }

    public void setHistoricalSignificance(String historicalSignificance) {
        this.historicalSignificance = historicalSignificance;
    }

    @Override
    public CoinCollection getCollection() {
        return collection;
    }

    public void setCollection(CoinCollection collection) {
        this.collection = collection;
    }
}
