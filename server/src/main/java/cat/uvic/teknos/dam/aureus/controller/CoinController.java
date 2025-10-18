package cat.uvic.teknos.dam.aureus.controller;

import cat.uvic.teknos.dam.aureus.impl.CoinImpl;
import cat.uvic.teknos.dam.aureus.service.CoinService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

public class CoinController {

    private final CoinService coinService;
    private final Gson gson;

    // Nuevo constructor que permite inyectar un Gson (útil para tests)
    public CoinController(CoinService coinService, Gson gson) {
        this.coinService = coinService;
        this.gson = gson;
    }

    // Constructor existente delega al nuevo, manteniendo comportamiento actual
    public CoinController(CoinService coinService) {
        this(coinService, new GsonBuilder().serializeNulls().create());
    }

    // Devuelve JSON de la lista de monedas
    public String getAllCoins() {
        List<CoinImpl> list = coinService.findAll();
        return gson.toJson(list);
    }

    // Devuelve JSON de una moneda por id, lanza EntityNotFoundException si no existe
    public String getCoin(int id) {
        CoinImpl coin = coinService.findById(id);
        return gson.toJson(coin);
    }

    // Crea una moneda a partir de JSON y devuelve el JSON creado (incluyendo id)
    public String createCoin(String body) {
        CoinImpl coin = gson.fromJson(body, CoinImpl.class);
        CoinImpl created = coinService.create(coin);
        return gson.toJson(created);
    }

    // Nuevo: Actualiza usando el id de la ruta (sobrescribe cualquier id en el body)
    public void updateCoin(int id, String body) {
        CoinImpl coin = gson.fromJson(body, CoinImpl.class);
        coin.setId(id);
        coinService.update(coin);
    }

    // Elimina una moneda por id
    public void deleteCoin(int id) {
        coinService.delete(id);
    }
}
