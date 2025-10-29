package cat.uvic.teknos.dam.aureus.model.jpa.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaRepositoryFactory {
    private final EntityManagerFactory entityManagerFactory;

    public JpaRepositoryFactory() {
        // Crear solo el EntityManagerFactory una vez; crear EntityManagers por petición
        this.entityManagerFactory = Persistence.createEntityManagerFactory("aureus");
    }

    public EntityManager createEntityManager() {
        return entityManagerFactory.createEntityManager();
    }

    public JpaCoinRepository getCoinRepository() {
        return new JpaCoinRepository(createEntityManager());
    }

    public JpaCollectionRepository getCollectionRepository() {
        return new JpaCollectionRepository(createEntityManager());
    }

    // Contenedor público para devolver repositorios que comparten el mismo EntityManager
    public static class SharedRepositories {
        public final JpaCoinRepository coinRepository;
        public final JpaCollectionRepository collectionRepository;

        public SharedRepositories(JpaCoinRepository coinRepository, JpaCollectionRepository collectionRepository) {
            this.coinRepository = coinRepository;
            this.collectionRepository = collectionRepository;
        }
    }

    // Crear y devolver ambos repositorios que usan el mismo EntityManager, sin exponer EntityManager
    public SharedRepositories createSharedRepositories() {
        var em = createEntityManager();
        return new SharedRepositories(new JpaCoinRepository(em), new JpaCollectionRepository(em));
    }

    public void close() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }
}
