package cat.uvic.teknos.dam.aureus.repositories.jdbc.model;

import cat.uvic.teknos.dam.aureus.CoinCollection;
import cat.uvic.teknos.dam.aureus.Coin;
import cat.uvic.teknos.dam.aureus.Collection;

public class JdbcCoinCollection implements CoinCollection {
    private Coin coin;
    private Collection collection;

    public JdbcCoinCollection() {}

    @Override
    public Integer getCoinId() {
        return coin != null ? coin.getId() : null;
    }

    @Override
    public Integer getCollectionId() {
        return collection != null ? collection.getId() : null;
    }

    @Override
    public Coin getCoin() {
        return coin;
    }

    @Override
    public void setCoin(Coin coin) {
        this.coin = coin;
    }

    @Override
    public Collection getCollection() {
        return collection;
    }

    @Override
    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    @Override
    public void setCoinId(Integer coinId) {
        if (this.coin != null) {
            this.coin.setId(coinId);
        }
    }

    @Override
    public void setCollectionId(Integer collectionId) {
        if (this.collection != null) {
            this.collection.setId(collectionId);
        }
    }
}
