package cat.uvic.teknos.dam.aureus.model.jpa;

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
public class JpaUserDetail {
    @Id
    @Column(name = "USER_ID")
    private int id;

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
}