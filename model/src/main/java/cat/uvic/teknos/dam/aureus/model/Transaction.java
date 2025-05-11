package cat.uvic.teknos.dam.aureus.model;

import java.sql.Timestamp;
import java.util.List;

public interface Transaction {
    Integer getTransactionId();
    String getMaterialName();
    Timestamp getTransactionDate();

    // Relación N:1 con User (comprador)
    User getBuyer();

    // Relación N:1 con User (vendedor)
    User getSeller();

    // Relación 1:N con CoinTransaction
    List<CoinTransaction> getCoinTransactions();
}
