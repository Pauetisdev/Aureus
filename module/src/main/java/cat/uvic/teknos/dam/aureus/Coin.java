package cat.uvic.teknos.dam.aureus;

import java.math.BigDecimal;

public class Coin {
    private int coinId;
    private String coinName;
    private int coinYear;
    private String coinMaterial;
    private BigDecimal coinWeight;
    private BigDecimal coinDiameter;
    private BigDecimal estimatedValue;
    private String originCountry;
    private String historicalSignificance;
    private int collectionId;

    public int getCoinId() {
        return coinId;
    }

    public void setCoinId(int coinId) {
        this.coinId = coinId;
    }

    public String getCoinName() {
        return coinName;
    }

    public void setCoinName(String coinName) {
        this.coinName = coinName;
    }

    public int getCoinYear() {
        return coinYear;
    }

    public void setCoinYear(int coinYear) {
        this.coinYear = coinYear;
    }

    public String getCoinMaterial() {
        return coinMaterial;
    }

    public void setCoinMaterial(String coinMaterial) {
        this.coinMaterial = coinMaterial;
    }

    public BigDecimal getCoinWeight() {
        return coinWeight;
    }

    public void setCoinWeight(BigDecimal coinWeight) {
        this.coinWeight = coinWeight;
    }

    public BigDecimal getCoinDiameter() {
        return coinDiameter;
    }

    public void setCoinDiameter(BigDecimal coinDiameter) {
        this.coinDiameter = coinDiameter;
    }

    public BigDecimal getEstimatedValue() {
        return estimatedValue;
    }

    public void setEstimatedValue(BigDecimal estimatedValue) {
        this.estimatedValue = estimatedValue;
    }

    public String getOriginCountry() {
        return originCountry;
    }

    public void setOriginCountry(String originCountry) {
        this.originCountry = originCountry;
    }

    public String getHistoricalSignificance() {
        return historicalSignificance;
    }

    public void setHistoricalSignificance(String historicalSignificance) {
        this.historicalSignificance = historicalSignificance;
    }

    public int getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(int collectionId) {
        this.collectionId = collectionId;
    }
}