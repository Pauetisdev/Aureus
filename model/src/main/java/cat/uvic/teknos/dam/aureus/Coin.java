package cat.uvic.teknos.dam.aureus;

public interface Coin {

    Integer getId();
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