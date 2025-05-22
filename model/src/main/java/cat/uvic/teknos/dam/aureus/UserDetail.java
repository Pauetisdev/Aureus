package cat.uvic.teknos.dam.aureus;

import java.sql.Date;

public interface UserDetail {
    int getId();

    Date getBirthday();
    String getPhone();
    String getGender();
    String getNationality();

    // Relación 1:1 con User
    User getUser();
}
