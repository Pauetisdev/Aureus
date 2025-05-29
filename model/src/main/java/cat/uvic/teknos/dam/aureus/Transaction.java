package cat.uvic.teknos.dam.aureus;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public interface Transaction {
    Integer getId();
    Timestamp getTransactionDate();
    User getBuyer();
    User getSeller();
    List<CoinTransaction> getCoinTransactions();

    void setId(Integer id);
    void setTransactionDate(Timestamp transactionDate);
    void setBuyer(User buyer);
    void setSeller(User seller);
    void setCoinTransactions(List<CoinTransaction> coinTransactions);
}
