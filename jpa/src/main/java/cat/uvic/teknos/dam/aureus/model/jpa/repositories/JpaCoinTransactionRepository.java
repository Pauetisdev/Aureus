package cat.uvic.teknos.dam.aureus.model.jpa.repositories;

import cat.uvic.teknos.dam.aureus.model.jpa.CoinTransactionId;
import cat.uvic.teknos.dam.aureus.model.jpa.JpaCoinTransaction;
import cat.uvic.teknos.dam.aureus.repositories.Repository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import java.util.HashSet;
import java.util.Set;

public class JpaCoinTransactionRepository implements Repository<CoinTransactionId, JpaCoinTransaction> {

    private final EntityManager em;

    public JpaCoinTransactionRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public void save(JpaCoinTransaction coinTransaction) {
        if (coinTransaction == null) {
            throw new InvalidDataException("CoinTransaction cannot be null");
        }
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Asegura que las entidades relacionadas estén gestionadas
            if (coinTransaction.getCoin() != null) {
                coinTransaction.setCoin(em.merge(coinTransaction.getCoin()));
            }
            if (coinTransaction.getTransaction() != null) {
                coinTransaction.setTransaction(em.merge(coinTransaction.getTransaction()));
            }
            if (em.find(JpaCoinTransaction.class, coinTransaction.getId()) == null) {
                em.persist(coinTransaction);
            } else {
                em.merge(coinTransaction);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new CrudException("Error saving coin transaction", e);
        }
    }

    @Override
    public void delete(JpaCoinTransaction coinTransaction) {
        if (coinTransaction == null) {
            throw new InvalidDataException("CoinTransaction cannot be null");
        }
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            JpaCoinTransaction ct = em.find(JpaCoinTransaction.class, coinTransaction.getId());
            if (ct == null) {
                throw new EntityNotFoundException("CoinTransaction with ID " + coinTransaction.getId() + " not found");
            }
            em.remove(ct);
            tx.commit();
        } catch (EntityNotFoundException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new CrudException("Error deleting coin transaction", e);
        }
    }

    @Override
    public JpaCoinTransaction get(CoinTransactionId id) {
        if (id == null) {
            throw new InvalidDataException("ID cannot be null");
        }
        try {
            JpaCoinTransaction coinTransaction = em.find(JpaCoinTransaction.class, id);
            if (coinTransaction == null) {
                throw new EntityNotFoundException("CoinTransaction with ID " + id + " not found");
            }
            return coinTransaction;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RepositoryException("Error retrieving coin transaction", e);
        }
    }

    @Override
    public Set<JpaCoinTransaction> getAll() {
        try {
            TypedQuery<JpaCoinTransaction> query = em.createQuery("SELECT ct FROM JpaCoinTransaction ct", JpaCoinTransaction.class);
            return new HashSet<>(query.getResultList());
        } catch (Exception e) {
            throw new RepositoryException("Error retrieving all coin transactions", e);
        }
    }
}