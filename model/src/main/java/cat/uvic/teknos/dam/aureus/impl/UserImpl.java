package cat.uvic.teknos.dam.aureus.impl;

import cat.uvic.teknos.dam.aureus.model.User;
import cat.uvic.teknos.dam.aureus.model.UserDetail;
import cat.uvic.teknos.dam.aureus.model.Collection;
import cat.uvic.teknos.dam.aureus.model.Transaction;
import java.sql.Timestamp;
import java.util.List;

public class UserImpl implements User {

    private Integer userId;
    private String username;
    private String email;
    private String passwordHash;
    private Timestamp joinDate;
    private UserDetail userDetail;
    private List<Collection> collections;
    private List<Transaction> buyerTransactions;
    private List<Transaction> sellerTransactions;

    public UserImpl(Integer userId, String username, String email, String passwordHash,
                    Timestamp joinDate, UserDetail userDetail, List<Collection> collections,
                    List<Transaction> buyerTransactions, List<Transaction> sellerTransactions) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.joinDate = joinDate;
        this.userDetail = userDetail;
        this.collections = collections;
        this.buyerTransactions = buyerTransactions;
        this.sellerTransactions = sellerTransactions;
    }

    @Override
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public Timestamp getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Timestamp joinDate) {
        this.joinDate = joinDate;
    }

    @Override
    public UserDetail getUserDetail() {
        return userDetail;
    }

    public void setUserDetail(UserDetail userDetail) {
        this.userDetail = userDetail;
    }

    @Override
    public List<Collection> getCollections() {
        return collections;
    }

    public void setCollections(List<Collection> collections) {
        this.collections = collections;
    }

    @Override
    public List<Transaction> getBuyerTransactions() {
        return buyerTransactions;
    }

    public void setBuyerTransactions(List<Transaction> buyerTransactions) {
        this.buyerTransactions = buyerTransactions;
    }

    @Override
    public List<Transaction> getSellerTransactions() {
        return sellerTransactions;
    }

    public void setSellerTransactions(List<Transaction> sellerTransactions) {
        this.sellerTransactions = sellerTransactions;
    }
}
