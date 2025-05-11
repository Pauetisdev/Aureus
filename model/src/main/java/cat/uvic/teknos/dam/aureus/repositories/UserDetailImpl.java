package cat.uvic.teknos.dam.aureus.repositories;

import cat.uvic.teknos.dam.aureus.model.UserDetail;
import cat.uvic.teknos.dam.aureus.model.User;
import java.sql.Date;

public class UserDetailImpl implements UserDetail {

    private int userId;
    private Date birthday;
    private String phone;
    private String gender;
    private String nationality;
    private User user; // Relación 1:1 con User

    public UserDetailImpl(int userId, Date birthday, String phone, String gender, String nationality, User user) {
        this.userId = userId;
        this.birthday = birthday;
        this.phone = phone;
        this.gender = gender;
        this.nationality = nationality;
        this.user = user;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public Date getBirthday() {
        return birthday;
    }

    @Override
    public String getPhone() {
        return phone;
    }

    @Override
    public String getGender() {
        return gender;
    }

    @Override
    public String getNationality() {
        return nationality;
    }

    @Override
    public User getUser() {
        return user;
    }
}
