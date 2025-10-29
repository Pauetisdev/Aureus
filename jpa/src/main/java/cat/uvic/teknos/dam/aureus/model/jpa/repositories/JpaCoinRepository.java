package cat.uvic.teknos.dam.aureus.model.jpa.repositories;

import cat.uvic.teknos.dam.aureus.model.jpa.JpaCoin;
import cat.uvic.teknos.dam.aureus.repositories.Repository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JpaCoinRepository implements Repository<Integer, JpaCoin> {
    private final EntityManager entityManager;

    public JpaCoinRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void save(JpaCoin coin) {
        if (coin == null) {
            throw new InvalidDataException("Coin cannot be null");
        }

        EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            if (coin.getId() == 0) {
                entityManager.persist(coin);
            } else {
                entityManager.merge(coin);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            // Log detailed information to help debugging: exception and root cause
            System.err.println("JpaCoinRepository.save: exception while saving coin (id=" + coin.getId() + "): " + e.getClass().getName() + ": " + e.getMessage());
            Throwable cause = e.getCause();
            while (cause != null) {
                System.err.println("  Caused by: " + cause.getClass().getName() + ": " + cause.getMessage());
                cause = cause.getCause();
            }
            e.printStackTrace(System.err);
            throw new CrudException("Error saving coin", e);
        }
    }

    @Override
    public void delete(JpaCoin coin) {
        if (coin == null) {
            throw new InvalidDataException("Coin cannot be null");
        }

        EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            JpaCoin coinToDelete = entityManager.find(JpaCoin.class, coin.getId());
            if (coinToDelete == null) {
                throw new EntityNotFoundException("Coin with ID " + coin.getId() + " not found");
            }
            entityManager.remove(coinToDelete);
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
            throw new CrudException("Error deleting coin", e);
        }
    }

    @Override
    public JpaCoin get(Integer id) {
        try {
            JpaCoin coin = entityManager.find(JpaCoin.class, id);
            if (coin == null) {
                throw new EntityNotFoundException("Coin with ID " + id + " not found");
            }
            return coin;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RepositoryException("Error retrieving coin", e);
        }
    }

    @Override
    public List<JpaCoin> getAll() {
        try {
            return entityManager.createQuery("FROM JpaCoin", JpaCoin.class).getResultList();
        } catch (Exception e) {
            throw new RepositoryException("Error retrieving all coins", e);
        }
    }


    public List<JpaCoin> findByMaterial(String material) {
        if (material == null || material.trim().isEmpty()) {
            throw new InvalidDataException("Material cannot be null or empty");
        }

        try {
            TypedQuery<JpaCoin> query = entityManager.createQuery(
                    "SELECT c FROM JpaCoin c WHERE c.coinMaterial = :material", JpaCoin.class);
            query.setParameter("material", material);
            return new ArrayList<>(query.getResultList());
        } catch (Exception e) {
            throw new RepositoryException("Error finding coins by material", e);
        }
    }

    public List<JpaCoin> findByYear(Integer year) {
        if (year == null) {
            throw new InvalidDataException("Year cannot be null");
        }

        try {
            TypedQuery<JpaCoin> query = entityManager.createQuery(
                    "SELECT c FROM JpaCoin c WHERE c.coinYear = :year", JpaCoin.class);
            query.setParameter("year", year);
            return new ArrayList<>(query.getResultList());
        } catch (Exception e) {
            throw new RepositoryException("Error finding coins by year", e);
        }
    }

    // Comprueba si la tabla COIN existe ejecutando una consulta nativa sencilla.
    public boolean isSchemaPresent() {
        try {
            // Intentar una consulta nativa: SELECT 1 FROM COIN LIMIT 1
            // Dependiendo del dialecto, LIMIT puede funcionar; en general, esto lanzará una excepción si la tabla no existe.
            entityManager.createNativeQuery("SELECT 1 FROM COIN LIMIT 1").getResultList();
            return true;
        } catch (Exception e) {
            // No imprimimos stacktrace extenso aquí para no ensuciar logs, pero sí lo registramos
            System.err.println("JpaCoinRepository.isSchemaPresent: schema check failed: " + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
    }

}