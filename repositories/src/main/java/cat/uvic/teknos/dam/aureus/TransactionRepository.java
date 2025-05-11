package cat.uvic.teknos.dam.aureus;

import cat.uvic.teknos.dam.aureus.model.Transaction;
import java.util.Set;

public interface TransactionRepository {
    void insert(Transaction transaction);
    void update(Transaction transaction);
    void delete(Transaction transaction);
    Transaction getById(Integer transactionId);
    Set<Transaction> getAll();
}
