package cat.uvic.teknos.dam.aureus.repositories.jdbc.model;

import cat.uvic.teknos.dam.aureus.CoinCollection;
import cat.uvic.teknos.dam.aureus.Collection;
import cat.uvic.teknos.dam.aureus.User;

import java.util.List;

public class JdbcCollection implements Collection {

    private Integer id;
    private String collectionName;
    private String description;
    private User user;
    private List<CoinCollection> coinCollections;

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(int id) {
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

    @Override
    public List<CoinCollection> getCoinCollections() {
        return coinCollections;
    }

    @Override
    public void setCoinCollections(List<CoinCollection> coinCollections) {
        this.coinCollections = coinCollections;
    }
}
