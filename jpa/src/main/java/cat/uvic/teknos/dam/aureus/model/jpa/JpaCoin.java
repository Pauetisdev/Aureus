package cat.uvic.teknos.dam.aureus.model.jpa;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "COIN")
@Data
@EqualsAndHashCode(exclude = {"collections", "coinTransactions"})
@ToString(exclude = {"collections", "coinTransactions"})
public class JpaCoin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COIN_ID")
    private int id;

    @Column(name = "COIN_NAME")
    private String coinName;

    @Column(name = "COIN_YEAR")
    private int coinYear;

    @Column(name = "COIN_MATERIAL")
    private String coinMaterial;

    @Column(name = "COIN_WEIGHT")
    private BigDecimal coinWeight;

    @Column(name = "COIN_DIAMETER")
    private BigDecimal coinDiameter;

    @Column(name = "ESTIMATED_VALUE")
    private BigDecimal estimatedValue;

    @Column(name = "ORIGIN_COUNTRY")
    private String originCountry;

    @Column(name = "HISTORICAL_SIGNIFICANCE")
    private String historicalSignificance;

    @ManyToMany(mappedBy = "coins")
    private Set<JpaCollection> collections = new HashSet<>();

    @OneToMany(mappedBy = "coin")
    private Set<JpaCoinTransaction> coinTransactions = new HashSet<>();
}
