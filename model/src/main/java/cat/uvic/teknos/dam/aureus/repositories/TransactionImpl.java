package cat.uvic.teknos.dam.aureus.repositories;

import cat.uvic.teknos.dam.aureus.model.Transaction;
import cat.uvic.teknos.dam.aureus.model.User;
import cat.uvic.teknos.dam.aureus.model.CoinTransaction;
import java.sql.Timestamp;
import java.util.List;

public class TransactionImpl implements Transaction {

    private Integer transactionId;
    private String materialName;
    private Timestamp transactionDate;
    private User buyer; // Relación N:1 con User (comprador)
    private User seller; // Relación N:1 con User (vendedor)
    private List<CoinTransaction> coinTransactions; // Relación 1:N con CoinTransaction

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

    @Override
    public String getMaterialName() {
        return materialName;
    }

    @Override
    public Timestamp getTransactionDate() {
        return transactionDate;
    }

    @Override
    public User getBuyer() {
        return buyer;
    }

    @Override
    public User getSeller() {
        return seller;
    }

    @Override
    public List<CoinTransaction> getCoinTransactions() {
        return coinTransactions;
    }
}
