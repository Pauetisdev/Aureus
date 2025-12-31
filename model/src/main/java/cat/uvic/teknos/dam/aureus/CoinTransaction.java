package cat.uvic.teknos.dam.aureus;

import java.math.BigDecimal;

public interface CoinTransaction {
    Coin getCoin();
    Transaction getTransaction();
    BigDecimal getTransactionPrice();
    String getCurrency();

    void setCoin(Coin coin);
    void setTransaction(Transaction transaction);
    void setTransactionPrice(java.math.BigDecimal transactionPrice);
    void setCurrency(String currency);

}
