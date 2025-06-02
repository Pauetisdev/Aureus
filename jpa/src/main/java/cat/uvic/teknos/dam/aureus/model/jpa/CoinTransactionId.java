package cat.uvic.teknos.dam.aureus.model.jpa;

import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoinTransactionId implements Serializable {
    private int coinId;
    private int transactionId;
}