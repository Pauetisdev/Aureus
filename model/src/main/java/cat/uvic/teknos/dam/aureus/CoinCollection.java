package cat.uvic.teknos.dam.aureus;

public interface CoinCollection {
    Integer getCoinId();
    Integer getCollectionId();
    Coin getCoin();
    Collection getCollection();

    void setCoinId(Integer coinId);
    void setCollectionId(Integer collectionId);
    void setCoin(Coin coin);
    void setCollection(Collection collection);

}
