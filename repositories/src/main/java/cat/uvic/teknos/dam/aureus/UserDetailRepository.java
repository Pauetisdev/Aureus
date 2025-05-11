package cat.uvic.teknos.dam.aureus;

import cat.uvic.teknos.dam.aureus.model.UserDetail;
import java.util.Set;

public interface UserDetailRepository {
    void insert(UserDetail userDetail);
    void update(UserDetail userDetail);
    void delete(UserDetail userDetail);
    UserDetail getById(Integer userDetailId);
    Set<UserDetail> getAll();
}
