package cat.uvic.teknos.dam.aureus.repositories;

import cat.uvic.teknos.dam.aureus.model.Coin;
import cat.uvic.teknos.dam.aureus.model.CoinCollection;
import cat.uvic.teknos.dam.aureus.model.Collection;

public class CoinCollectionImpl implements CoinCollection {

    private Integer coinId;
    private Integer collectionId;
    private Coin coin; // Relación N:1 con Coin
    private Collection collection; // Relación N:1 con Collection

    public CoinCollectionImpl(Integer coinId, Integer collectionId, Coin coin, Collection collection) {
        this.coinId = coinId;
        this.collectionId = collectionId;
        this.coin = coin;
        this.collection = collection;
    }

    @Override
    public Integer getCoinId() {
        return coinId;
    }

    @Override
    public Integer getCollectionId() {
        return collectionId;
    }

    @Override
    public Coin getCoin() {
        return coin;
    }

    @Override
    public Collection getCollection() {
        return collection;
    }
}
