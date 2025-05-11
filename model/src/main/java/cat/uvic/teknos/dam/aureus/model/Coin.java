package cat.uvic.teknos.dam.aureus.model;

public interface Coin {

    Integer getCoinId();
    String getCoinName();
    Integer getCoinYear();
    String getCoinMaterial();
    Double getCoinWeight();
    Double getCoinDiameter();
    Double getEstimatedValue();
    String getOriginCountry();
    String getHistoricalSignificance();
    CoinCollection getCollection();
}