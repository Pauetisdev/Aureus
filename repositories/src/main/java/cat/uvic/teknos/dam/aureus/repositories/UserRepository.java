package cat.uvic.teknos.dam.aureus;

import cat.uvic.teknos.dam.aureus.model.User;
import java.util.Set;

public interface UserRepository {
    void insert(User user);
    void update(User user);
    void delete(User user);
    User getById(Integer userId);
    Set<User> getAll();
}