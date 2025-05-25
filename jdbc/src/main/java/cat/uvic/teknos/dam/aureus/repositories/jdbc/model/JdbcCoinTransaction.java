package cat.uvic.teknos.dam.aureus.repositories.jdbc.model;

import cat.uvic.teknos.dam.aureus.CoinTransaction;
import cat.uvic.teknos.dam.aureus.Coin;
import cat.uvic.teknos.dam.aureus.Transaction;

import java.math.BigDecimal;

public class JdbcCoinTransaction implements CoinTransaction {
    private Coin coin;
    private Transaction transaction;
    private BigDecimal transactionPrice;
    private String currency;

    public JdbcCoinTransaction() {}

    @Override
    public Coin getCoin() {
        return coin;
    }
    @Override
    public void setCoin(Coin coin) {
        this.coin = coin;
    }

    @Override
    public Transaction getTransaction() {
        return transaction;
    }
    @Override
    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public BigDecimal getTransactionPrice() {
        return transactionPrice;
    }
    @Override
    public void setTransactionPrice(BigDecimal transactionPrice) {
        this.transactionPrice = transactionPrice;
    }

    @Override
    public String getCurrency() {
        return currency;
    }
    @Override
    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
