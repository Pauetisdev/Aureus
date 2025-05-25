package cat.uvic.teknos.dam.aureus.repositories;

import cat.uvic.teknos.dam.aureus.Transaction;

import java.util.List;
import java.sql.Timestamp;

public interface TransactionRepository extends Repository<Integer, Transaction> {
    List<Transaction> findByDateRange(Timestamp start, Timestamp end);
}
