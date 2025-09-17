package cat.uvic.teknos.dam.aureus.model.jpa;

import cat.uvic.teknos.dam.aureus.User;
import cat.uvic.teknos.dam.aureus.UserDetail;
import cat.uvic.teknos.dam.aureus.Collection;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "USER")
@Data
@EqualsAndHashCode(exclude = {"collections", "userDetail"})
@ToString(exclude = {"collections", "userDetail"})
public class JpaUser implements User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    private Integer id;

    @Column(name = "USERNAME",nullable = false)
    private String username;

    @Column(name = "EMAIL", unique = true, nullable = false)
    private String email;

    @Column(name = "PASSWORD_HASH", nullable = false)
    private String passwordHash;

    @Column(name = "JOIN_DATE")
    private LocalDateTime joinDate;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private JpaUserDetail userDetail;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<JpaCollection> collections = new ArrayList<>();

    @Override
    public List<Collection> getCollections() {
        return collections.stream()
                .map(c -> (Collection) c)
                .collect(Collectors.toList());
    }

    @Override
    public void setCollections(List<Collection> collections) {
        this.collections.clear();
        for (Collection c : collections) {
            if (c instanceof JpaCollection) {
                this.collections.add((JpaCollection) c);
            } else {
                throw new IllegalArgumentException("Solo se pueden añadir instancias de JpaCollection");
            }
        }
    }

    @Override
    public UserDetail getUserDetail() {
        return userDetail;
    }

    @Override
    public void setUserDetail(UserDetail userDetail) {
        if (userDetail instanceof JpaUserDetail) {
            this.userDetail = (JpaUserDetail) userDetail;
        } else {
            throw new IllegalArgumentException("Solo se puede asignar una instancia de JpaUserDetail");
        }
    }
}