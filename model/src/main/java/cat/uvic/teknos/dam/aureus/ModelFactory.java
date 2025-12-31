package cat.uvic.teknos.dam.aureus;

public interface ModelFactory {
    Coin newCoin();
    CoinCollection newCoinCollection();
    CoinTransaction newCoinTransaction();
    Collection newCollection();
    Transaction newTransaction();
    User newUser();
    UserDetail newUserDetail();
}
