package cat.uvic.teknos.dam.aureus.model.jpa.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaRepositoryFactory {
    private final EntityManagerFactory entityManagerFactory;
    private final EntityManager entityManager;

    public JpaRepositoryFactory() {
        this.entityManagerFactory = Persistence.createEntityManagerFactory("aureus");
        this.entityManager = entityManagerFactory.createEntityManager();
    }

    public JpaCoinRepository getCoinRepository() {
        return new JpaCoinRepository(entityManager);
    }

    public JpaCollectionRepository getCollectionRepository() {
        return new JpaCollectionRepository(entityManager);
    }

    public void close() {
        if (entityManager != null && entityManager.isOpen()) {
            entityManager.close();
        }
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }
}
