package cat.uvic.teknos.dam.aureus.repositories;

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
    private UserDetail userDetail; // Relación 1:1 con UserDetail
    private List<Collection> collections; // Relación 1:N con Collection
    private List<Transaction> buyerTransactions; // Relaciones como comprador
    private List<Transaction> sellerTransactions; // Relaciones como vendedor

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

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getPasswordHash() {
        return passwordHash;
    }

    @Override
    public Timestamp getJoinDate() {
        return joinDate;
    }

    @Override
    public UserDetail getUserDetail() {
        return userDetail;
    }

    @Override
    public List<Collection> getCollections() {
        return collections;
    }

    @Override
    public List<Transaction> getBuyerTransactions() {
        return buyerTransactions;
    }

    @Override
    public List<Transaction> getSellerTransactions() {
        return sellerTransactions;
    }
}
