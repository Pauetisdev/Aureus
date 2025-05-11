package cat.uvic.teknos.dam.aureus;

import cat.uvic.teknos.dam.aureus.model.CoinCollection;
import java.util.Set;

public interface CoinCollectionRepository {
    void insert(CoinCollection coinCollection);
    void update(CoinCollection coinCollection);
    void delete(CoinCollection coinCollection);
    CoinCollection getById(Integer coinCollectionId);
    Set<CoinCollection> getAll();
}
