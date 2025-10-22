package cat.uvic.teknos.dam.aureus.model.jpa.repositories;

import cat.uvic.teknos.dam.aureus.CoinTransaction;
import cat.uvic.teknos.dam.aureus.repositories.CoinTransactionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class JpaCoinTransactionRepository implements CoinTransactionRepository {
    private final EntityManager entityManager;

    public JpaCoinTransactionRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void save(CoinTransaction entity) {
        EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            entityManager.merge(entity);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error saving CoinTransaction", e);
        }
    }

    @Override
    public void delete(CoinTransaction entity) {
        EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error deleting CoinTransaction", e);
        }
    }

    @Override
    public CoinTransaction get(Integer id) {
        return entityManager.find(CoinTransaction.class, id);
    }

    @Override
    public List<CoinTransaction> getAll() {
        TypedQuery<CoinTransaction> query = entityManager.createQuery(
            "SELECT ct FROM CoinTransaction ct", CoinTransaction.class);
        return query.getResultList();
    }
}

