package cat.uvic.teknos.dam.aureus;

import java.time.LocalDate;

public interface UserDetail {
    Integer getId();
    void setId(Integer id);

    LocalDate getBirthdate();
    void setBirthdate(LocalDate birthdate);

    String getPhone();
    void setPhone(String phone);

    String getGender();
    void setGender(String gender);

    String getNationality();
    void setNationality(String nationality);

    User getUser();
    void setUser(User user);
}