package cat.uvic.teknos.dam.aureus;

import java.time.LocalDate;
import java.util.List;

public interface Collection {
    Integer getId();
    void setId(Integer id);

    String getCollectionName();
    void setCollectionName(String collectionName);

    String getDescription();
    void setDescription(String description);

    User getUser();
    void setUser(User user);

    List<CoinCollection> getCoinCollections();
    void setCoinCollections(List<CoinCollection> coinCollections);
}