package cat.uvic.teknos.dam.aureus;

import java.time.LocalDateTime;
import java.util.List;

public interface User {
    Integer getId();
    void setId(Integer id);

    String getUsername();
    void setUsername(String username);

    String getEmail();
    void setEmail(String email);

    String getPasswordHash();
    void setPasswordHash(String passwordHash);

    LocalDateTime getJoinDate();
    void setJoinDate(LocalDateTime joinDate);

    UserDetail getUserDetail();
    void setUserDetail(UserDetail userDetail);

    List<Collection> getCollections();
    void setCollections(List<Collection> collections);
}