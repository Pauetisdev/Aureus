package cat.uvic.teknos.dam.aureus.model.jpa;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "COLLECTION")
@Data
@EqualsAndHashCode(exclude = {"user", "coins"})
@ToString(exclude = {"user", "coins"})
public class JpaCollection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COLLECTION_ID")
    private int id;

    @Column(name = "NAME")
    private String name;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private JpaUser user;

    @ManyToMany
    @JoinTable(
            name = "COINS_COLLECTION",
            joinColumns = @JoinColumn(name = "COLLECTION_ID"),
            inverseJoinColumns = @JoinColumn(name = "COIN_ID")
    )
    private Set<JpaCoin> coins = new HashSet<>();
}
