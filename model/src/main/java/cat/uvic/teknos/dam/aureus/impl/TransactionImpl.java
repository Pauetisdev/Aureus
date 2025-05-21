package cat.uvic.teknos.dam.aureus.impl;

import cat.uvic.teknos.dam.aureus.model.Transaction;
import cat.uvic.teknos.dam.aureus.model.User;
import cat.uvic.teknos.dam.aureus.model.CoinTransaction;
import java.sql.Timestamp;
import java.util.List;

public class TransactionImpl implements Transaction {

    private Integer transactionId;
    private String materialName;
    private Timestamp transactionDate;
    private User buyer;
    private User seller;
    private List<CoinTransaction> coinTransactions;

    public TransactionImpl(Integer transactionId, String materialName, Timestamp transactionDate,
                           User buyer, User seller, List<CoinTransaction> coinTransactions) {
        this.transactionId = transactionId;
        this.materialName = materialName;
        this.transactionDate = transactionDate;
        this.buyer = buyer;
        this.seller = seller;
        this.coinTransactions = coinTransactions;
    }

    @Override
    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    @Override
    public Timestamp getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Timestamp transactionDate) {
        this.transactionDate = transactionDate;
    }

    @Override
    public User getBuyer() {
        return buyer;
    }

    public void setBuyer(User buyer) {
        this.buyer = buyer;
    }

    @Override
    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }

    @Override
    public List<CoinTransaction> getCoinTransactions() {
        return coinTransactions;
    }

    public void setCoinTransactions(List<CoinTransaction> coinTransactions) {
        this.coinTransactions = coinTransactions;
    }
}
