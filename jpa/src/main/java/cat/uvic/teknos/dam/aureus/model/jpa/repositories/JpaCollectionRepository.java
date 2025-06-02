package cat.uvic.teknos.dam.aureus.model.jpa.repositories;

import cat.uvic.teknos.dam.aureus.model.jpa.JpaCollection;
import cat.uvic.teknos.dam.aureus.repositories.Repository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.HashSet;
import java.util.Set;

public class JpaCollectionRepository implements Repository<Integer, JpaCollection> {
    private final EntityManager entityManager;

    public JpaCollectionRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void save(JpaCollection collection) {
        if (collection == null) {
            throw new InvalidDataException("Collection cannot be null");
        }

        EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            if (collection.getId() == 0) {
                entityManager.persist(collection);
            } else {
                entityManager.merge(collection);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new CrudException("Error saving collection", e);
        }
    }

    @Override
    public void delete(JpaCollection collection) {
        if (collection == null) {
            throw new InvalidDataException("Collection cannot be null");
        }

        EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            JpaCollection collectionToDelete = entityManager.find(JpaCollection.class, collection.getId());
            if (collectionToDelete == null) {
                throw new EntityNotFoundException("Collection with ID " + collection.getId() + " not found");
            }
            entityManager.remove(collectionToDelete);
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
            throw new CrudException("Error deleting collection", e);
        }
    }

    @Override
    public JpaCollection get(Integer id) {
        try {
            JpaCollection collection = entityManager.find(JpaCollection.class, id);
            if (collection == null) {
                throw new EntityNotFoundException("Collection with ID " + id + " not found");
            }
            return collection;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RepositoryException("Error retrieving collection", e);
        }
    }

    @Override
    public Set<JpaCollection> getAll() {
        try {
            return new HashSet<>(entityManager.createQuery("FROM JpaCollection", JpaCollection.class).getResultList());
        } catch (Exception e) {
            throw new RepositoryException("Error retrieving all collections", e);
        }
    }
}