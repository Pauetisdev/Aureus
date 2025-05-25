package cat.uvic.teknos.dam.aureus.repositories.jdbc.model;

import cat.uvic.teknos.dam.aureus.Transaction;
import cat.uvic.teknos.dam.aureus.User;
import cat.uvic.teknos.dam.aureus.CoinTransaction;

import java.sql.Timestamp;
import java.util.List;

public class JdbcTransaction implements Transaction {
    private Integer id;
    private String materialName;
    private Timestamp transactionDate;
    private User buyer;
    private User seller;
    private List<CoinTransaction> coinTransactions;

    public JdbcTransaction() {}

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String getMaterialName() {
        return materialName;
    }

    @Override
    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    @Override
    public Timestamp getTransactionDate() {
        return transactionDate;
    }

    @Override
    public void setTransactionDate(Timestamp transactionDate) {
        this.transactionDate = transactionDate;
    }

    @Override
    public User getBuyer() {
        return buyer;
    }

    @Override
    public void setBuyer(User buyer) {
        this.buyer = buyer;
    }

    @Override
    public User getSeller() {
        return seller;
    }

    @Override
    public void setSeller(User seller) {
        this.seller = seller;
    }

    @Override
    public List<CoinTransaction> getCoinTransactions() {
        return coinTransactions;
    }

    @Override
    public void setCoinTransactions(List<CoinTransaction> coinTransactions) {
        this.coinTransactions = coinTransactions;
    }
}
