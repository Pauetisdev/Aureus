package cat.uvic.teknos.dam.aureus;

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
    Collection getCollection(); // Relación N:1 con Collection
}
