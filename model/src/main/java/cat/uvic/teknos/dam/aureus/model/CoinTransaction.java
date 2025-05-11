package cat.uvic.teknos.dam.aureus.model;

import java.math.BigDecimal;

public interface CoinTransaction {
    // Relación N:1 con Coin
    Coin getCoin();

    // Relación N:1 con Transaction
    Transaction getTransaction();

    BigDecimal getTransactionPrice();
    String getCurrency();
}
