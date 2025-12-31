package cat.uvic.teknos.dam.aureus.repositories.jdbc.factories;

import cat.uvic.teknos.dam.aureus.User;
import cat.uvic.teknos.dam.aureus.UserDetail;
import cat.uvic.teknos.dam.aureus.Collection;
import cat.uvic.teknos.dam.aureus.Coin;
import cat.uvic.teknos.dam.aureus.Transaction;
import cat.uvic.teknos.dam.aureus.CoinCollection;
import cat.uvic.teknos.dam.aureus.CoinTransaction;

// JDBC CLASSES
import cat.uvic.teknos.dam.aureus.repositories.jdbc.model.*;

public class ModelFactory {

    public static User createUser() {
        return new JdbcUser();
    }

    public static UserDetail createUserDetail() {
        return new JdbcUserDetail();
    }

    public static Collection createCollection() {
        return new JdbcCollection();
    }

    public static Coin createCoin() {
        return new JdbcCoin();
    }

    public static Transaction createTransaction() {
        return new JdbcTransaction();
    }

    public static CoinCollection createCoinCollection() {
        return new JdbcCoinCollection();
    }

    public static CoinTransaction createCoinTransaction() {
        return new JdbcCoinTransaction();
    }
}
