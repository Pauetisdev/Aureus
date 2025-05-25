package cat.uvic.teknos.dam.aureus.repositories.jdbc.model;

import cat.uvic.teknos.dam.aureus.Collection;
import cat.uvic.teknos.dam.aureus.Transaction;
import cat.uvic.teknos.dam.aureus.User;
import cat.uvic.teknos.dam.aureus.UserDetail;

import java.sql.Timestamp;
import java.util.List;

public class JdbcUser implements User {

    private Integer id;
    private String username;
    private String email;
    private String passwordHash;
    private Timestamp joinDate;
    private UserDetail userDetail;
    private List<Collection> collections;
    private List<Transaction> buyerTransactions;
    private List<Transaction> sellerTransactions;

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getPasswordHash() {
        return passwordHash;
    }

    @Override
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public Timestamp getJoinDate() {
        return joinDate;
    }

    @Override
    public void setJoinDate(Timestamp joinDate) {
        this.joinDate = joinDate;
    }

    @Override
    public UserDetail getUserDetail() {
        return userDetail;
    }

    @Override
    public void setUserDetail(UserDetail userDetail) {
        this.userDetail = userDetail;
    }

    @Override
    public List<Collection> getCollections() {
        return collections;
    }

    @Override
    public void setCollections(List<Collection> collections) {
        this.collections = collections;
    }

    @Override
    public List<Transaction> getBuyerTransactions() {
        return buyerTransactions;
    }

    @Override
    public void setBuyerTransactions(List<Transaction> buyerTransactions) {
        this.buyerTransactions = buyerTransactions;
    }

    @Override
    public List<Transaction> getSellerTransactions() {
        return sellerTransactions;
    }

    @Override
    public void setSellerTransactions(List<Transaction> sellerTransactions) {
        this.sellerTransactions = sellerTransactions;
    }
}
