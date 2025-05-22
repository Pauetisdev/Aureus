package cat.uvic.teknos.dam.aureus.impl;

import cat.uvic.teknos.dam.aureus.Collection;
import cat.uvic.teknos.dam.aureus.User;
import cat.uvic.teknos.dam.aureus.CoinCollection;
import java.util.List;

public class CollectionImpl implements Collection {

    private int id;
    private String collectionName;
    private String description;
    private User user;
    private List<CoinCollection> coinCollections;

    public CollectionImpl(int id, String collectionName, String description, User user, List<CoinCollection> coinCollections) {
        this.id = id;
        this.collectionName = collectionName;
        this.description = description;
        this.user = user;
        this.coinCollections = coinCollections;
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID de colección debe ser positivo");
        }
        this.id = id;
    }

    @Override
    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        if (collectionName == null || collectionName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la colección no puede estar vacío");
        }
        this.collectionName = collectionName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        // La descripción puede ser opcional (null o vacío)
        this.description = description;
    }

    @Override
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("El usuario asociado no puede ser nulo");
        }
        this.user = user;
    }

    @Override
    public List<CoinCollection> getCoinCollections() {
        return coinCollections;
    }

    public void setCoinCollections(List<CoinCollection> coinCollections) {
        if (coinCollections == null) {
            throw new IllegalArgumentException("La lista de monedas no puede ser nula");
        }
        this.coinCollections = coinCollections;
    }
}
