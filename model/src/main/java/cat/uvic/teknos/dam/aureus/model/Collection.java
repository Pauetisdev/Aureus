package cat.uvic.teknos.dam.aureus.model;

import java.util.List;

public interface Collection {
    int getCollectionId();
    String getCollectionName();
    String getDescription();

    // Relación N:1 con User
    User getUser();

    // Relación N:N con Coin a través de CoinCollection
    List<CoinCollection> getCoinCollections();
}
