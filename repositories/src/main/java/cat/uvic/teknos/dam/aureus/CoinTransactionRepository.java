package cat.uvic.teknos.dam.aureus;

import cat.uvic.teknos.dam.aureus.model.CoinTransaction;
import java.util.Set;

public interface CoinTransactionRepository {
    void insert(CoinTransaction coinTransaction);
    void update(CoinTransaction coinTransaction);
    void delete(CoinTransaction coinTransaction);
    CoinTransaction getById(Integer coinTransactionId);
    Set<CoinTransaction> getAll();
}
