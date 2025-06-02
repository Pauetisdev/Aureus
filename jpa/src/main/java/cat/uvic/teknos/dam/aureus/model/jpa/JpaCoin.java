package cat.uvic.teknos.dam.aureus.model.jpa;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "COIN")
@Data
// @EqualsAndHashCode, @ToString : To avoid infinite recursion
@EqualsAndHashCode(exclude = {"collection", "coinTransactions"})
@ToString(exclude = {"collection", "coinTransactions"})
public class JpaCoin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COIN_ID")
    private int id;

    @Column(name = "COIN_NAME", nullable = false)
    private String coinName;

    @Column(name = "COIN_YEAR")
    private Integer coinYear;

    @Column(name = "COIN_MATERIAL")
    private String coinMaterial;

    @Column(name = "COIN_WEIGHT", precision = 10, scale = 2)
    private BigDecimal coinWeight;

    @Column(name = "COIN_DIAMETER", precision = 10, scale = 2)
    private BigDecimal coinDiameter;

    @Column(name = "ESTIMATED_VALUE", precision = 15, scale = 2)
    private BigDecimal estimatedValue;

    @Column(name = "ORIGIN_COUNTRY")
    private String originCountry;

    @Column(name = "HISTORICAL_SIGNIFICANCE", length = 1000)
    private String historicalSignificance;

    // RELATION WITH COLLECTION (Many-to-One)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COLLECTION_ID")
    private JpaCollection collection;

    // RELATION WITH COIN_TRANSACTIONS (One-to-Many)
    @OneToMany(mappedBy = "coin", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<JpaCoinTransaction> coinTransactions = new ArrayList<>();
}