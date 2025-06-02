package cat.uvic.teknos.dam.aureus.impl;

import cat.uvic.teknos.dam.aureus.Collection;
import cat.uvic.teknos.dam.aureus.User;
import cat.uvic.teknos.dam.aureus.UserDetail;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserImpl implements User {

    private Integer id;
    private String username;
    private String email;
    private String passwordHash;
    private LocalDateTime joinDate;
    private UserDetail userDetail;
    private List<Collection> collections;

    public UserImpl() {
        this.collections = new ArrayList<>();
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getPasswordHash() {
        return passwordHash;
    }

    @Override
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public LocalDateTime getJoinDate() {
        return joinDate;
    }

    @Override
    public void setJoinDate(LocalDateTime joinDate) {
        this.joinDate = joinDate;
    }

    @Override
    public UserDetail getUserDetail() {
        return userDetail;
    }

    @Override
    public void setUserDetail(UserDetail userDetail) {
        this.userDetail = userDetail;
    }

    @Override
    public List<Collection> getCollections() {
        return collections;
    }

    @Override
    public void setCollections(List<Collection> collections) {
        this.collections = collections != null ? collections : new ArrayList<>();
    }
}