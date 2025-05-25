package cat.uvic.teknos.dam.aureus.impl;

import cat.uvic.teknos.dam.aureus.*;

public class ModelFactoryImpl implements ModelFactory {

    @Override
    public Coin newCoin() {
        return new CoinImpl();
    }

    @Override
    public CoinCollection newCoinCollection() {
        return new CoinCollectionImpl();
    }

    @Override
    public CoinTransaction newCoinTransaction() {
        return new CoinTransactionImpl();
    }

    @Override
    public Collection newCollection() {
        return new CollectionImpl();
    }

    @Override
    public Transaction newTransaction() {
        return new TransactionImpl();
    }

    @Override
    public User newUser() {
        return new UserImpl();
    }

    @Override
    public UserDetail newUserDetail() {
        return new UserDetailImpl();
    }
}
