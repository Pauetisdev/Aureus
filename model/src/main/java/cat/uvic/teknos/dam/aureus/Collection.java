package cat.uvic.teknos.dam.aureus;

import java.util.List;

public interface Collection {
    Integer getId();
    String getCollectionName();
    String getDescription();
    User getUser();
    List<CoinCollection> getCoinCollections();

    void setId(int id);
    void setCollectionName(String collectionName);
    void setDescription(String description);
    void setUser(User user);
    void setCoinCollections(java.util.List<CoinCollection> coinCollections);

}
