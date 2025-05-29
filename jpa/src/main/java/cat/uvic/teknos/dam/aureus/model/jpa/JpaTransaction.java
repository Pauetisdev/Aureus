package cat.uvic.teknos.dam.aureus.model.jpa;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "TRANSACTION")
@Data
@EqualsAndHashCode(exclude = {"coinTransactions", "buyer", "seller"})
@ToString(exclude = {"coinTransactions", "buyer", "seller"})
public class JpaTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TRANSACTION_ID")
    private int id;

    @Column(name = "TRANSACTION_DATE")
    private java.sql.Timestamp transactionDate;

    @ManyToOne
    @JoinColumn(name = "BUYER_ID")
    private JpaUser buyer;

    @ManyToOne
    @JoinColumn(name = "SELLER_ID")
    private JpaUser seller;

    @OneToMany(mappedBy = "transaction")
    private Set<JpaCoinTransaction> coinTransactions = new HashSet<>();
}