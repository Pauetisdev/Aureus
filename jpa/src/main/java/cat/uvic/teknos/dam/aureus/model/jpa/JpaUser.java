package cat.uvic.teknos.dam.aureus.model.jpa;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "USER")
@Data
// @EqualsAndHashCode, @ToString: para evitar recursion infinita
@EqualsAndHashCode(exclude = {"collections", "userDetail"})
@ToString(exclude = {"collections", "userDetail"})

public class JpaUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    private int id;

    @Column(name = "NAME")
    private String name;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "USER_DETAIL_ID")
    private JpaUserDetail userDetail;

    @OneToMany(mappedBy = "user")
    private Set<JpaCollection> collections = new HashSet<>();
}