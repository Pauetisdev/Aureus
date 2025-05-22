package cat.uvic.teknos.dam.aureus.impl;

import cat.uvic.teknos.dam.aureus.UserDetail;
import cat.uvic.teknos.dam.aureus.User;
import java.sql.Date;

public class UserDetailImpl implements UserDetail {

    private int id;
    private Date birthday;
    private String phone;
    private String gender;
    private String nationality;
    private User user; // Relación 1:1 con User

    public UserDetailImpl(int id, Date birthday, String phone, String gender, String nationality, User user) {
        this.id = id;
        this.birthday = birthday;
        this.phone = phone;
        this.gender = gender;
        this.nationality = nationality;
        this.user = user;
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    @Override
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    @Override
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
