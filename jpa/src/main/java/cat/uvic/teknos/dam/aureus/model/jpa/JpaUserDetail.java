package cat.uvic.teknos.dam.aureus.model.jpa;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "USER_DETAIL")
@Data
@EqualsAndHashCode(exclude = "user")
@ToString(exclude = "user")
public class JpaUserDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_DETAIL_ID")
    private int id;

    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "PHONE")
    private String phone;

    @OneToOne(mappedBy = "userDetail")
    private JpaUser user;
}