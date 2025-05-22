package cat.uvic.teknos.dam.aureus.impl;

import cat.uvic.teknos.dam.aureus.Coin;
import cat.uvic.teknos.dam.aureus.CoinCollection;

public class CoinImpl implements Coin {

    private Integer id;
    private String coinName;
    private Integer coinYear;
    private String coinMaterial;
    private Double coinWeight;
    private Double coinDiameter;
    private Double estimatedValue;
    private String originCountry;
    private String historicalSignificance;
    private CoinCollection collection;

    // Constructor
    public CoinImpl(Integer id, String coinName, Integer coinYear, String coinMaterial,
                    Double coinWeight, Double coinDiameter, Double estimatedValue,
                    String originCountry, String historicalSignificance, CoinCollection collection) {
        if (id == null || coinName == null || coinMaterial == null || originCountry == null || historicalSignificance == null) {
            throw new IllegalArgumentException("Cap camp pot ser null.");
        }
        this.id = id;
        this.coinName = coinName;
        this.coinYear = coinYear;
        this.coinMaterial = coinMaterial;
        this.coinWeight = coinWeight;
        this.coinDiameter = coinDiameter;
        this.estimatedValue = estimatedValue;
        this.originCountry = originCountry;
        this.historicalSignificance = historicalSignificance;
        this.collection = collection;
    }


    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("id no puede ser null");
        }
        this.id = id;
    }


    @Override
    public String getCoinName() {
        return coinName;
    }

    public void setCoinName(String coinName) {
        if (coinName == null) {
            throw new IllegalArgumentException("coinName no puede ser null");
        }
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
        if (coinMaterial == null) {
            throw new IllegalArgumentException("coinMaterial no puede ser null");
        }
        this.coinMaterial = coinMaterial;
    }


    @Override
    public Double getCoinWeight() {
        return coinWeight;
    }

    public void setCoinWeight(Double coinWeight) {
        this.coinWeight = coinWeight;
    }


    @Override
    public Double getCoinDiameter() {
        return coinDiameter;
    }

    public void setCoinDiameter(Double coinDiameter) {
        this.coinDiameter = coinDiameter;
    }


    @Override
    public Double getEstimatedValue() {
        return estimatedValue;
    }

    public void setEstimatedValue(Double estimatedValue) {
        this.estimatedValue = estimatedValue;
    }


    @Override
    public String getOriginCountry() {
        return originCountry;
    }

    public void setOriginCountry(String originCountry) {
        if (originCountry == null) {
            throw new IllegalArgumentException("originCountry no puede ser null");
        }
        this.originCountry = originCountry;
    }


    @Override
    public String getHistoricalSignificance() {
        return historicalSignificance;
    }

    public void setHistoricalSignificance(String historicalSignificance) {
        if (historicalSignificance == null) {
            throw new IllegalArgumentException("historicalSignificance no puede ser null");
        }
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