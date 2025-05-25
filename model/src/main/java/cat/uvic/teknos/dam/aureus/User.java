package cat.uvic.teknos.dam.aureus;

import java.sql.Timestamp;
import java.util.List;

public interface User {
    Integer getId();
    String getUsername();
    String getEmail();
    String getPasswordHash();
    Timestamp getJoinDate();
    UserDetail getUserDetail();
    List<Collection> getCollections();
    List<Transaction> getBuyerTransactions();
    List<Transaction> getSellerTransactions();

    void setId(Integer id);
    void setUsername(String username);
    void setEmail(String email);
    void setPasswordHash(String passwordHash);
    void setJoinDate(java.sql.Timestamp joinDate);
    void setUserDetail(UserDetail userDetail);
    void setCollections(java.util.List<Collection> collections);
    void setBuyerTransactions(java.util.List<Transaction> buyerTransactions);
    void setSellerTransactions(java.util.List<Transaction> sellerTransactions);

}
