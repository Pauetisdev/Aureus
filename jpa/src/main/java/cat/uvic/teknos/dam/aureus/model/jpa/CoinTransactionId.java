package cat.uvic.teknos.dam.aureus.model.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CoinTransactionId implements Serializable {
    @Column(name = "COIN_ID")
    private Integer coinId;

    @Column(name = "TRANSACTION_ID")
    private Integer transactionId;

    // Constructor vacío
    public CoinTransactionId() {}

    // Constructor con parámetros
    public CoinTransactionId(Integer coinId, Integer transactionId) {
        this.coinId = coinId;
        this.transactionId = transactionId;
    }

    // Getters y setters
    public Integer getCoinId() { return coinId; }
    public void setCoinId(Integer coinId) { this.coinId = coinId; }
    public Integer getTransactionId() { return transactionId; }
    public void setTransactionId(Integer transactionId) { this.transactionId = transactionId; }

    // equals y hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CoinTransactionId)) return false;
        CoinTransactionId that = (CoinTransactionId) o;
        return Objects.equals(coinId, that.coinId) &&
                Objects.equals(transactionId, that.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coinId, transactionId);
    }
}