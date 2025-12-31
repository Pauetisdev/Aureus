package cat.uvic.teknos.dam.aureus.model.jpa.repositories;

import cat.uvic.teknos.dam.aureus.User;
import cat.uvic.teknos.dam.aureus.model.jpa.JpaUser;
import cat.uvic.teknos.dam.aureus.repositories.UserRepository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class JpaUserRepository implements UserRepository {
    private final EntityManager entityManager;

    public JpaUserRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void save(User user) {
        if (user == null) {
            throw new InvalidDataException("User cannot be null");
        }

        EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            if (user.getId() == null) {
                entityManager.persist(user);
            } else {
                entityManager.merge(user);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new CrudException("Error saving user", e);
        }
    }

    @Override
    public void delete(User user) {
        if (user == null) {
            throw new InvalidDataException("User cannot be null");
        }

        EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            JpaUser userToDelete = entityManager.find(JpaUser.class, user.getId());
            if (userToDelete == null) {
                throw new EntityNotFoundException("User with ID " + user.getId() + " not found");
            }
            entityManager.remove(userToDelete);
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
            throw new CrudException("Error deleting user", e);
        }
    }

    @Override
    public User get(Integer id) {
        try {
            JpaUser user = entityManager.find(JpaUser.class, id);
            if (user == null) {
                throw new EntityNotFoundException("User with ID " + id + " not found");
            }
            return user;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RepositoryException("Error retrieving user", e);
        }
    }

    @Override
    public List<User> getAll() {
        try {
            List<JpaUser> jpaUsers = entityManager.createQuery("SELECT u FROM JpaUser u", JpaUser.class).getResultList();
            return jpaUsers.stream().map(u -> (User) u).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RepositoryException("Error retrieving all users", e);
        }
    }


    @Override
    public User findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new InvalidDataException("Email cannot be null or empty");
        }

        try {
            TypedQuery<JpaUser> query = entityManager.createQuery(
                    "SELECT u FROM JpaUser u WHERE u.email = :email", JpaUser.class);
            query.setParameter("email", email);
            return query.getSingleResult();
        } catch (NoResultException e) {
            throw new EntityNotFoundException("User with email '" + email + "' not found");
        } catch (Exception e) {
            throw new RepositoryException("Error retrieving user by email", e);
        }
    }
}