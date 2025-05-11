package cat.uvic.teknos.dam.aureus;

import cat.uvic.teknos.dam.aureus.model.Collection;
import java.util.Set;

public interface CollectionRepository {
    void insert(Collection collection);
    void update(Collection collection);
    void delete(Collection collection);
    Collection getById(Integer collectionId);
    Set<Collection> getAll();
}
