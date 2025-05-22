package cat.uvic.teknos.dam.aureus;

public interface CoinCollection {
    Integer getCoinId();
    Integer getCollectionId();

    // Relación N:1 con Coin
    Coin getCoin();

    // Relación N:1 con Collection
    Collection getCollection();
}
