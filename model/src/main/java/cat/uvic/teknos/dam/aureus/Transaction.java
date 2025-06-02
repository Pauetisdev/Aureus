package cat.uvic.teknos.dam.aureus;

import java.sql.Timestamp;
import java.util.List;

public interface Transaction {
    Integer getId();
    void setId(Integer id);

    Timestamp getTransactionDate();
    void setTransactionDate(Timestamp transactionDate);

    User getBuyer();
    void setBuyer(User buyer);

    User getSeller();
    void setSeller(User seller);

    List<CoinTransaction> getCoinTransactions();
    void setCoinTransactions(List<CoinTransaction> coinTransactions);
}