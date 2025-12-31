package cat.uvic.teknos.dam.aureus.impl;

import cat.uvic.teknos.dam.aureus.Collection;
import cat.uvic.teknos.dam.aureus.CoinCollection;
import cat.uvic.teknos.dam.aureus.User;

import java.util.ArrayList;
import java.util.List;

public class CollectionImpl implements Collection {

    private Integer id;
    private String collectionName;
    private String description;
    private User user;
    private List<CoinCollection> coinCollections;

    public CollectionImpl() {
        this.coinCollections = new ArrayList<>();
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String getCollectionName() {
        return collectionName;
    }

    @Override
    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void setUser(User user) {
        this.user = user;
    }
}