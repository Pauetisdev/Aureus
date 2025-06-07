package cat.uvic.teknos.dam.aureus.impl;

import cat.uvic.teknos.dam.aureus.Coin;
import cat.uvic.teknos.dam.aureus.CoinCollection;
import cat.uvic.teknos.dam.aureus.Collection;

public class CoinCollectionImpl implements CoinCollection {

    private Integer coinId;
    private Integer collectionId;
    private Coin coin;
    private Collection collection;
    private Integer id;

    public CoinCollectionImpl() {}

    @Override
    public Integer getCoinId() {
        return coinId;
    }

    public void setCoinId(Integer coinId) {
        this.coinId = coinId;
    }

    @Override
    public Integer getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Integer collectionId) {
        this.collectionId = collectionId;
    }

    @Override
    public Coin getCoin() {
        return coin;
    }

    public void setCoin(Coin coin) {
        this.coin = coin;
    }

    @Override
    public Collection getCollection() {
        return collection;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }
}
