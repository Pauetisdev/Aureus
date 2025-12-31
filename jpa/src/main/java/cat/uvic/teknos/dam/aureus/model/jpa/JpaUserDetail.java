package cat.uvic.teknos.dam.aureus.model.jpa;

import cat.uvic.teknos.dam.aureus.UserDetail;
import cat.uvic.teknos.dam.aureus.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.time.LocalDate;

@Entity
@Table(name = "USER_DETAIL")
@Data
@EqualsAndHashCode(exclude = "user")
@ToString(exclude = "user")
public class JpaUserDetail implements UserDetail {
    @Id
    @Column(name = "USER_ID")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "USER_ID")
    private JpaUser user;

    @Column(name = "BIRTHDATE")
    private LocalDate birthdate;

    @Column(name = "PHONE")
    private String phone;

    @Column(name = "GENDER")
    private String gender;

    @Column(name = "NATIONALITY")
    private String nationality;

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void setUser(User user) {
        if (user instanceof JpaUser) {
            this.user = (JpaUser) user;
        } else {
            throw new IllegalArgumentException("Solo se puede asignar una instancia de JpaUser");
        }
    }
}