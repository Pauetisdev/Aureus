package cat.uvic.teknos.dam.aureus.repositories;

import cat.uvic.teknos.dam.aureus.model.Coin;
import cat.uvic.teknos.dam.aureus.model.CoinCollection;

public class CoinImpl implements Coin {

    private Integer coinId;
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
    public CoinImpl(Integer coinId, String coinName, Integer coinYear, String coinMaterial,
                    Double coinWeight, Double coinDiameter, Double estimatedValue,
                    String originCountry, String historicalSignificance, CoinCollection collection) {

        // Validación de parámetros
        if (coinId == null || coinName == null || coinMaterial == null || originCountry == null || historicalSignificance == null) {
            throw new IllegalArgumentException("Ningún campo puede ser null");
        }

        this.coinId = coinId;
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
    public Integer getCoinId() {
        return coinId;
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
    public Double getCoinWeight() {
        return coinWeight;
    }

    @Override
    public Double getCoinDiameter() {
        return coinDiameter;
    }

    @Override
    public Double getEstimatedValue() {
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
}