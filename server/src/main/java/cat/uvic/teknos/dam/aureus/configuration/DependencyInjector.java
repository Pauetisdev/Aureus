package cat.uvic.teknos.dam.aureus.configuration;

import cat.uvic.teknos.dam.aureus.controller.CoinController;
import cat.uvic.teknos.dam.aureus.controller.CollectionController;
import cat.uvic.teknos.dam.aureus.core.Server;
import cat.uvic.teknos.dam.aureus.http.RequestRouter;
import cat.uvic.teknos.dam.aureus.model.jpa.repositories.JpaRepositoryFactory;
import cat.uvic.teknos.dam.aureus.model.jpa.repositories.JpaCoinRepository;
import cat.uvic.teknos.dam.aureus.service.CoinService;
import cat.uvic.teknos.dam.aureus.service.JpaCoinService;

/**
 * Minimal dependency provider used to create and wire server-side components.
 *
 * <p>This class centralizes the construction of controllers, services and
 * the embedded server so tests and the main application can obtain fully
 * configured instances without using a DI container.</p>
 */
public class DependencyInjector {

    private static JpaRepositoryFactory repositoryFactory;

    private static JpaRepositoryFactory getRepositoryFactory() {
        if (repositoryFactory == null) {
            repositoryFactory = new JpaRepositoryFactory();
        }
        return repositoryFactory;
    }

    /**
     * Provide a {@link CoinService} implementation wired to the JPA repositories.
     *
     * @return configured CoinService
     */
    public static CoinService provideCoinService() {
        JpaRepositoryFactory factory = getRepositoryFactory();
        // Crear repositorios que comparten el mismo EntityManager (sin exponer EntityManager fuera del módulo jpa)
        var shared = factory.createSharedRepositories();
        JpaCoinRepository coinRepository = shared.coinRepository;
        var collRepo = shared.collectionRepository;
        return new JpaCoinService(coinRepository, collRepo);
    }

    /**
     * Provide a {@link CoinController} configured with a {@link CoinService}.
     *
     * @return configured CoinController
     */
    public static CoinController provideCoinController() {
        return new CoinController(provideCoinService());
    }

    /**
     * Provide a {@link CoinController} backed by the provided service.
     * Useful for tests that inject mocks or alternative implementations.
     *
     * @param coinService coin service instance to use
     * @return CoinController instance
     */
    public static CoinController provideCoinController(CoinService coinService) {
        return new CoinController(coinService);
    }

    /**
     * Provide a {@link CollectionController} backed by the JPA collection repository.
     *
     * @return configured CollectionController
     */
    public static CollectionController provideCollectionController() {
        return new CollectionController(getRepositoryFactory().getCollectionRepository());
    }

    public static RequestRouter provideRequestRouter() {
        return new RequestRouter(provideCoinController(), provideCollectionController());
    }

    public static RequestRouter provideRequestRouter(CoinController coinController) {
        return new RequestRouter(coinController, provideCollectionController());
    }

    public static Server provideServer(int port) {
        return new Server(port, provideRequestRouter());
    }

    public static Server provideServer(int port, RequestRouter router) {
        return new Server(port, router);
    }
}
