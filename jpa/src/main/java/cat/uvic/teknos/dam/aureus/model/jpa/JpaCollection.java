package cat.uvic.teknos.dam.aureus.model.jpa;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "COLLECTION_NAME", nullable = false)
    private String collectionName;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private JpaUser user;

    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<JpaCoin> coins = new ArrayList<>();
}
