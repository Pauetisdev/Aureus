package cat.uvic.teknos.dam.aureus.model.jpa;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.math.BigDecimal;

@Entity
@Table(name = "COIN_TRANSACTION")
@Data
@EqualsAndHashCode(exclude = {"coin", "transaction"})
@ToString(exclude = {"coin", "transaction"})
public class JpaCoinTransaction {
    @EmbeddedId
    private CoinTransactionId id;

    @ManyToOne
    @MapsId("coinId")
    @JoinColumn(name = "COIN_ID")
    private JpaCoin coin;

    @ManyToOne
    @MapsId("transactionId")
    @JoinColumn(name = "TRANSACTION_ID")
    private JpaTransaction transaction;

    @Column(name = "TRANSACTION_PRICE")
    private BigDecimal transactionPrice;

    @Column(name = "CURRENCY")
    private String currency;
}