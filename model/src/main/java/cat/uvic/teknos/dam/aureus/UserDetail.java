package cat.uvic.teknos.dam.aureus;

import java.sql.Date;

public interface UserDetail {
    Integer getId();
    Date getBirthday();
    String getPhone();
    String getGender();
    String getNationality();
    User getUser();

    void setId(int id);
    void setBirthday(java.sql.Date birthday);
    void setPhone(String phone);
    void setGender(String gender);
    void setNationality(String nationality);
    void setUser(User user);

}
