package cat.uvic.teknos.dam.aureus;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public interface Transaction {
    Integer getId();
    String getMaterialName();
    Timestamp getTransactionDate();
    User getBuyer();
    User getSeller();
    List<CoinTransaction> getCoinTransactions();
    String getType();
    Date getDate();
    String getLocation();
    String getNotes();

    void setId(Integer id);
    void setMaterialName(String materialName);
    void setTransactionDate(java.sql.Timestamp transactionDate);
    void setBuyer(User buyer);
    void setSeller(User seller);
    void setCoinTransactions(java.util.List<CoinTransaction> coinTransactions);
    void setType(String type);
    void setDate(Date date);
    void setLocation(String location);
    void setNotes(String notes);

}
