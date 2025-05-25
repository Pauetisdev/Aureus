package cat.uvic.teknos.dam.aureus.impl;

import cat.uvic.teknos.dam.aureus.Transaction;
import cat.uvic.teknos.dam.aureus.User;
import cat.uvic.teknos.dam.aureus.CoinTransaction;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class TransactionImpl implements Transaction {

    private Integer id;
    private String materialName;
    private Timestamp transactionDate;
    private User buyer;
    private User seller;
    private List<CoinTransaction> coinTransactions;
    private String type;
    private Date date;
    private String location;
    private String notes;

    public TransactionImpl() {
        this.id = id;
        this.materialName = materialName;
        this.transactionDate = transactionDate;
        this.buyer = buyer;
        this.seller = seller;
        this.coinTransactions = coinTransactions;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public String getNotes() {
        return notes;
    }

    public void setCoinTransactions(List<CoinTransaction> coinTransactions) {
        this.coinTransactions = coinTransactions;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public void setNotes(String notes) {
        this.notes = notes;
    }
}