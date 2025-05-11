package cat.uvic.teknos.dam.aureus.model;

import java.sql.Timestamp;
import java.util.List;

public interface User {
    Integer getUserId();
    String getUsername();
    String getEmail();
    String getPasswordHash();
    Timestamp getJoinDate();

    // Relación 1:1 con UserDetail
    UserDetail getUserDetail();

    // Relación 1:N con Collection
    List<Collection> getCollections();

    // Relaciones como comprador/vendedor
    List<Transaction> getBuyerTransactions();
    List<Transaction> getSellerTransactions();
}
