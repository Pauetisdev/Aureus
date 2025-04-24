package cat.uvic.teknos.dam.aureus.impl;

import cat.uvic.teknos.dam.aureus.Coin;
import cat.uvic.teknos.dam.aureus.Collection;

public class CoinImpl implements Coin {
    @Override
    public Integer getCoinId() {
        return 0;
    }

    @Override
    public String getCoinName() {
        return "";
    }

    @Override
    public Integer getCoinYear() {
        return 0;
    }

    @Override
    public String getCoinMaterial() {
        return "";
    }

    @Override
    public Double getCoinWeight() {
        return 0.0;
    }

    @Override
    public Double getCoinDiameter() {
        return 0.0;
    }

    @Override
    public Double getEstimatedValue() {
        return 0.0;
    }

    @Override
    public String getOriginCountry() {
        return "";
    }

    @Override
    public String getHistoricalSignificance() {
        return "";
    }

    @Override
    public Collection getCollection() {
        return null;
    }
}