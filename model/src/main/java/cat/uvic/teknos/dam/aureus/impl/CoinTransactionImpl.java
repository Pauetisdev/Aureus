package cat.uvic.teknos.dam.aureus.impl;

import cat.uvic.teknos.dam.aureus.Coin;
import cat.uvic.teknos.dam.aureus.CoinTransaction;
import cat.uvic.teknos.dam.aureus.Transaction;
import java.math.BigDecimal;

public class CoinTransactionImpl implements CoinTransaction {

    private Coin coin;
    private Transaction transaction;
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

    public void setCoin(Coin coin) {
        if (coin == null) {
            throw new IllegalArgumentException("Coin no puede ser null");
        }
        this.coin = coin;
    }

    @Override
    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction no puede ser null");
        }
        this.transaction = transaction;
    }

    @Override
    public BigDecimal getTransactionPrice() {
        return transactionPrice;
    }

    public void setTransactionPrice(BigDecimal transactionPrice) {
        if (transactionPrice == null || transactionPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio de transacción debe ser positivo");
        }
        this.transactionPrice = transactionPrice;
    }

    @Override
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        if (currency == null || currency.trim().isEmpty()) {
            throw new IllegalArgumentException("La moneda no puede estar vacía");
        }
        this.currency = currency;
    }
}