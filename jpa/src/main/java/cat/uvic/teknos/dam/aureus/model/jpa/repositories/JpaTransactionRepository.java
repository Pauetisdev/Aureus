package cat.uvic.teknos.dam.aureus.model.jpa.repositories;

import cat.uvic.teknos.dam.aureus.model.jpa.JpaTransaction;
import cat.uvic.teknos.dam.aureus.repositories.Repository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JpaTransactionRepository implements Repository<Integer, JpaTransaction> {
    private final EntityManager entityManager;

    public JpaTransactionRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void save(JpaTransaction transaction) {
        if (transaction == null) {
            throw new InvalidDataException("Transaction cannot be null");
        }

        EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            if (transaction.getId() == 0) {
                entityManager.persist(transaction);
            } else {
                entityManager.merge(transaction);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new CrudException("Error saving transaction", e);
        }
    }

    @Override
    public void delete(JpaTransaction transaction) {
        if (transaction == null) {
            throw new InvalidDataException("Transaction cannot be null");
        }

        EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            JpaTransaction transactionToDelete = entityManager.find(JpaTransaction.class, transaction.getId());
            if (transactionToDelete == null) {
                throw new EntityNotFoundException("Transaction with ID " + transaction.getId() + " not found");
            }
            entityManager.remove(transactionToDelete);
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
            throw new CrudException("Error deleting transaction", e);
        }
    }

    @Override
    public JpaTransaction get(Integer id) {
        try {
            JpaTransaction transaction = entityManager.find(JpaTransaction.class, id);
            if (transaction == null) {
                throw new EntityNotFoundException("Transaction with ID " + id + " not found");
            }
            return transaction;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RepositoryException("Error retrieving transaction", e);
        }
    }

    @Override
    public List<JpaTransaction> getAll() {
        try {
            return entityManager.createQuery("SELECT t FROM JpaTransaction t", JpaTransaction.class).getResultList();
        } catch (Exception e) {
            throw new RepositoryException("Error retrieving all transactions", e);
        }
    }


    public List<JpaTransaction> findByDateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new InvalidDataException("Start and end timestamps cannot be null");
        }
        if (start.isAfter(end)) {
            throw new InvalidDataException("Start timestamp must be before end timestamp");
        }

        try {
            TypedQuery<JpaTransaction> query = entityManager.createQuery(
                    "SELECT t FROM JpaTransaction t WHERE t.transactionDate BETWEEN :start AND :end ORDER BY t.transactionDate",
                    JpaTransaction.class);
            query.setParameter("start", start);
            query.setParameter("end", end);
            return new ArrayList<>(query.getResultList());
        } catch (Exception e) {
            throw new RepositoryException("Error finding transactions by date range", e);
        }
    }
}