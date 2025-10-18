package cat.uvic.teknos.dam.aureus.configuration;

import cat.uvic.teknos.dam.aureus.controller.CoinController;
import cat.uvic.teknos.dam.aureus.core.Server;
import cat.uvic.teknos.dam.aureus.http.RequestRouter;
import cat.uvic.teknos.dam.aureus.service.CoinService;
import cat.uvic.teknos.dam.aureus.service.CoinServiceImpl;

public class DependencyInjector {

    public static CoinService provideCoinService() {
        return new CoinServiceImpl();
    }

    public static CoinController provideCoinController() {
        return new CoinController(provideCoinService());
    }

    public static CoinController provideCoinController(CoinService coinService) {
        return new CoinController(coinService);
    }

    public static RequestRouter provideRequestRouter() {
        return new RequestRouter(provideCoinController());
    }

    public static RequestRouter provideRequestRouter(CoinController coinController) {
        return new RequestRouter(coinController);
    }

    public static Server provideServer(int port) {
        return new Server(port, provideRequestRouter());
    }

    public static Server provideServer(int port, RequestRouter router) {
        return new Server(port, router);
    }
}
