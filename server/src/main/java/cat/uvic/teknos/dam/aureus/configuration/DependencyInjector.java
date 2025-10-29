package cat.uvic.teknos.dam.aureus.configuration;

import cat.uvic.teknos.dam.aureus.controller.CoinController;
import cat.uvic.teknos.dam.aureus.controller.CollectionController;
import cat.uvic.teknos.dam.aureus.core.Server;
import cat.uvic.teknos.dam.aureus.http.RequestRouter;
import cat.uvic.teknos.dam.aureus.model.jpa.repositories.JpaRepositoryFactory;
import cat.uvic.teknos.dam.aureus.model.jpa.repositories.JpaCoinRepository;
import cat.uvic.teknos.dam.aureus.service.CoinService;
import cat.uvic.teknos.dam.aureus.service.JpaCoinService;

public class DependencyInjector {

    private static JpaRepositoryFactory repositoryFactory;

    private static JpaRepositoryFactory getRepositoryFactory() {
        if (repositoryFactory == null) {
            repositoryFactory = new JpaRepositoryFactory();
        }
        return repositoryFactory;
    }

    public static CoinService provideCoinService() {
        JpaRepositoryFactory factory = getRepositoryFactory();
        // Crear repositorios que comparten el mismo EntityManager (sin exponer EntityManager fuera del módulo jpa)
        var shared = factory.createSharedRepositories();
        JpaCoinRepository coinRepository = shared.coinRepository;
        var collRepo = shared.collectionRepository;
        return new JpaCoinService(coinRepository, collRepo);
    }

    public static CoinController provideCoinController() {
        return new CoinController(provideCoinService());
    }

    public static CoinController provideCoinController(CoinService coinService) {
        return new CoinController(coinService);
    }

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
