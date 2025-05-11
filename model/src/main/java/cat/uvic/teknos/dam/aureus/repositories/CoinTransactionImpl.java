package cat.uvic.teknos.dam.aureus.repositories;

import cat.uvic.teknos.dam.aureus.model.Coin;
import cat.uvic.teknos.dam.aureus.model.CoinTransaction;
import cat.uvic.teknos.dam.aureus.model.Transaction;
import java.math.BigDecimal;

public class CoinTransactionImpl implements CoinTransaction {

    private Coin coin; // Relación N:1 con Coin
    private Transaction transaction; // Relación N:1 con Transaction
    private BigDecimal transactionPrice;
    private String currency;

    public CoinTransactionImpl(Coin coin, Transaction transaction, BigDecimal transactionPrice, String currency) {
        this.coin = coin;
        this.transaction = transaction;
        this.transactionPrice = transactionPrice;
        this.currency = currency;
    }

    @Override
    public Coin getCoin() {
        return coin;
    }

    @Override
    public Transaction getTransaction() {
        return transaction;
    }

    @Override
    public BigDecimal getTransactionPrice() {
        return transactionPrice;
    }

    @Override
    public String getCurrency() {
        return currency;
    }
}
