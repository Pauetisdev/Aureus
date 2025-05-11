package cat.uvic.teknos.dam.aureus.repositories;

import cat.uvic.teknos.dam.aureus.model.Collection;
import cat.uvic.teknos.dam.aureus.model.User;
import cat.uvic.teknos.dam.aureus.model.CoinCollection;
import java.util.List;

public class CollectionImpl implements Collection {

    private int collectionId;
    private String collectionName;
    private String description;
    private User user; // Relación N:1 con User
    private List<CoinCollection> coinCollections; // Relación N:N con Coin a través de CoinCollection

    public CollectionImpl(int collectionId, String collectionName, String description, User user, List<CoinCollection> coinCollections) {
        this.collectionId = collectionId;
        this.collectionName = collectionName;
        this.description = description;
        this.user = user;
        this.coinCollections = coinCollections;
    }

    @Override
    public int getCollectionId() {
        return collectionId;
    }

    @Override
    public String getCollectionName() {
        return collectionName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public List<CoinCollection> getCoinCollections() {
        return coinCollections;
    }
}
