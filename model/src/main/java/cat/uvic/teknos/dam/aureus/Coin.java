package cat.uvic.teknos.dam.aureus;

import java.math.BigDecimal;

public interface Coin {

    Integer getId();
    String getCoinName();
    Integer getCoinYear();
    String getCoinMaterial();
    BigDecimal getCoinWeight();
    BigDecimal getCoinDiameter();
    BigDecimal getEstimatedValue();
    String getOriginCountry();
    String getHistoricalSignificance();
    CoinCollection getCollection();

    void setId(Integer id);
    void setCoinName(String coinName);
    void setCoinYear(Integer coinYear);
    void setCoinMaterial(String coinMaterial);
    void setCoinWeight(BigDecimal coinWeight);
    void setCoinDiameter(BigDecimal coinDiameter);
    void setEstimatedValue(BigDecimal estimatedValue);
    void setOriginCountry(String originCountry);
    void setHistoricalSignificance(String historicalSignificance);
    void setCollection(CoinCollection collection);

}