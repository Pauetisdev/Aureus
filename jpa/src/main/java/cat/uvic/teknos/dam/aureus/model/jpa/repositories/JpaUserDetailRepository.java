package cat.uvic.teknos.dam.aureus.model.jpa.repositories;

import cat.uvic.teknos.dam.aureus.model.jpa.JpaUserDetail;
import cat.uvic.teknos.dam.aureus.repositories.Repository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.HashSet;
import java.util.List;

@SuppressWarnings("unused")
public class JpaUserDetailRepository implements Repository<Integer, JpaUserDetail> {
    private final EntityManager entityManager;

    public JpaUserDetailRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void save(JpaUserDetail detail) {
        if (detail == null) {
            throw new InvalidDataException("UserDetail cannot be null");
        }
        if (detail.getUser() == null || detail.getUser().getId() == null) {
            throw new InvalidDataException("UserDetail must be associated with a persisted User");
        }

        EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            if (detail.getId() == null) {
                entityManager.persist(detail);
            } else {
                entityManager.merge(detail);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new CrudException("Error saving user detail", e);
        }
    }

    @Override
    public void delete(JpaUserDetail detail) {
        if (detail == null) {
            throw new InvalidDataException("UserDetail cannot be null");
        }

        EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            JpaUserDetail detailToDelete = entityManager.find(JpaUserDetail.class, detail.getId());
            if (detailToDelete == null) {
                throw new EntityNotFoundException("UserDetail with ID " + detail.getId() + " not found");
            }
            entityManager.remove(detailToDelete);
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
            throw new CrudException("Error deleting user detail", e);
        }
    }

    @Override
    public JpaUserDetail get(Integer id) {
        try {
            JpaUserDetail detail = entityManager.find(JpaUserDetail.class, id);
            if (detail == null) {
                throw new EntityNotFoundException("UserDetail with ID " + id + " not found");
            }
            return detail;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RepositoryException("Error retrieving user detail", e);
        }
    }

    @Override
    public List<JpaUserDetail> getAll() {
        try {
            return entityManager.createQuery("FROM JpaUserDetail", JpaUserDetail.class).getResultList();
        } catch (Exception e) {
            throw new RepositoryException("Error retrieving all user details", e);
        }
    }

}