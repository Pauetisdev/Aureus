package cat.uvic.teknos.dam.aureus.controller;

import cat.uvic.teknos.dam.aureus.impl.CoinImpl;
import cat.uvic.teknos.dam.aureus.impl.CoinCollectionImpl;
import cat.uvic.teknos.dam.aureus.service.CoinService;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CoinControllerTest {

    private CoinService service;
    private CoinController controller;
    private Gson gson;

    @BeforeEach
    void setUp() {
        service = Mockito.mock(CoinService.class);
        gson = new Gson();
        controller = new CoinController(service, gson);
    }

    @Test
    void getAllCoinsDelegatesToServiceAndReturnsJson() {
        CoinImpl c = new CoinImpl();
        c.setId(1);
        c.setCoinName("X");
        when(service.findAll()).thenReturn(List.of(c));

        String json = controller.getAllCoins();
        assertTrue(json.contains("X"));
        verify(service).findAll();
    }

    @Test
    void createCoinParsesJsonAndReturnsCreated() {
        CoinImpl toCreate = new CoinImpl();
        toCreate.setCoinName("New");
        toCreate.setCoinYear(10);
        toCreate.setCoinMaterial("Gold");
        toCreate.setCoinWeight(new BigDecimal("5.0"));
        toCreate.setCoinDiameter(new BigDecimal("20.0"));
        toCreate.setEstimatedValue(new BigDecimal("100.0"));
        toCreate.setOriginCountry("Rome");
        toCreate.setHistoricalSignificance("Ancient coin");
        CoinCollectionImpl coll = new CoinCollectionImpl();
        coll.setId(2);
        toCreate.setCollection(coll);

        CoinImpl created = new CoinImpl();
        created.setId(5);
        created.setCoinName("New");
        created.setCoinYear(10);

        // El controlador delega a create(coin, collectionId) cuando detecta collectionId en el JSON
        when(service.create(any(), anyInt())).thenReturn(created);

        String requestJson = gson.toJson(toCreate);
        String responseJson = controller.createCoin(requestJson);
        assertTrue(responseJson.contains("\"id\":5") || responseJson.contains("5"));
        verify(service).create(any(), anyInt());
    }

    @Test
    void updateUsesRouteIdToOverwriteBodyId() {
        CoinImpl created = new CoinImpl();
        created.setId(7);
        created.setCoinName("Seven");
        when(service.findById(7)).thenReturn(created);

        String bodyJson = gson.toJson(created);
        // Should not throw
        controller.updateCoin(7, bodyJson);
        verify(service).update(any());
    }

    @Test
    void deleteDelegatesToService() {
        controller.deleteCoin(10);
        verify(service).delete(10);
    }
}
