package cat.uvic.teknos.dam.aureus.controller;

import cat.uvic.teknos.dam.aureus.CoinCollection;
import cat.uvic.teknos.dam.aureus.impl.CoinCollectionImpl;
import cat.uvic.teknos.dam.aureus.impl.CoinImpl;
import cat.uvic.teknos.dam.aureus.service.CoinService;
import cat.uvic.teknos.dam.aureus.http.exception.HttpException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.lang.reflect.Method;
import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CoinController {

    private final CoinService coinService;
    private final Gson gson;

    // Gson auxiliar para parsear interfaces concretas (CoinCollection)
    private static final Gson parsingGson = new GsonBuilder()
            .registerTypeAdapter(CoinCollection.class, (InstanceCreator<CoinCollection>) type -> new CoinCollectionImpl())
            .create();

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
        // Validación a nivel de JSON para evitar problemas de deserialización parcial
        validateJsonForCreate(body);
        // Luego deserializar ya sabiendo que los campos obligatorios existen
        CoinImpl coin = parsingGson.fromJson(body, CoinImpl.class);
        CoinImpl created = coinService.create(coin);
        return gson.toJson(created);
    }

    private void validateJsonForCreate(String body) {
        JsonElement parsed = JsonParser.parseString(body);
        if (!parsed.isJsonObject()) throw new HttpException(400, "Bad Request", "Request body must be a JSON object");
        JsonObject obj = parsed.getAsJsonObject();

        List<String> missing = new ArrayList<>();
        if (!hasNonEmptyString(obj, "coinName")) missing.add("coinName");
        if (!obj.has("coinYear") || obj.get("coinYear").isJsonNull()) missing.add("coinYear");
        if (!hasNonEmptyString(obj, "coinMaterial")) missing.add("coinMaterial");
        if (!obj.has("coinWeight") || obj.get("coinWeight").isJsonNull()) missing.add("coinWeight");
        if (!obj.has("coinDiameter") || obj.get("coinDiameter").isJsonNull()) missing.add("coinDiameter");
        if (!obj.has("estimatedValue") || obj.get("estimatedValue").isJsonNull()) missing.add("estimatedValue");
        if (!hasNonEmptyString(obj, "originCountry")) missing.add("originCountry");
        // historicalSignificance is optional per DB rules - do not require it here

        // Validar collection.id o collection.collectionId
        if (!obj.has("collection") || obj.get("collection").isJsonNull()) {
            missing.add("collectionId");
        } else {
            JsonObject coll = obj.getAsJsonObject("collection");
            boolean hasId = (coll.has("id") && !coll.get("id").isJsonNull()) || (coll.has("collectionId") && !coll.get("collectionId").isJsonNull());
            if (!hasId) missing.add("collectionId");
        }

        if (!missing.isEmpty()) {
            String msg = "Missing required fields: " + String.join(", ", missing);
            throw new HttpException(400, "Bad Request", msg);
        }
    }

    private boolean hasNonEmptyString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() && obj.get(key).isJsonPrimitive() && !obj.get(key).getAsString().trim().isEmpty();
    }

    // Nuevo: Actualiza usando el id de la ruta (sobrescribe cualquier id en el body)
    public void updateCoin(int id, String body) {
        // usar parsingGson para poder instanciar la implementación de CoinCollection si viene en el body
        CoinImpl coin = parsingGson.fromJson(body, CoinImpl.class);
        coin.setId(id);
        coinService.update(coin);
    }

    // Elimina una moneda por id
    public void deleteCoin(int id) {
        coinService.delete(id);
    }

    // Validación de los campos obligatorios para creación
    private void validateForCreate(CoinImpl coin) {
        List<String> missing = new ArrayList<>();
        if (coin == null) {
            throw new HttpException(400, "Bad Request", "Request body is empty or malformed");
        }
        if (coin.getCoinName() == null || coin.getCoinName().trim().isEmpty()) missing.add("coinName");
        if (coin.getCoinYear() == null) missing.add("coinYear");
        if (coin.getCoinMaterial() == null || coin.getCoinMaterial().trim().isEmpty()) missing.add("coinMaterial");
        if (coin.getCoinWeight() == null) missing.add("coinWeight");
        if (coin.getCoinDiameter() == null) missing.add("coinDiameter");
        if (coin.getEstimatedValue() == null) missing.add("estimatedValue");
        if (coin.getOriginCountry() == null || coin.getOriginCountry().trim().isEmpty()) missing.add("originCountry");
        // historicalSignificance is optional per DB rules - do not require it here

        Integer extractedCollectionId = extractCollectionId(coin.getCollection());
        if (extractedCollectionId == null) missing.add("collectionId");

        if (!missing.isEmpty()) {
            String msg = "Missing required fields: " + String.join(", ", missing);
            throw new HttpException(400, "Bad Request", msg);
        }
    }

    private Integer extractCollectionId(CoinCollection collection) {
        if (collection == null) return null;
        try {
            // Si Gson deserializó la colección como un Map (LinkedTreeMap), extraer claves
            if (collection instanceof Map) {
                Map<?,?> m = (Map<?,?>) collection;
                Object v = m.get("id");
                if (v instanceof Number) return ((Number) v).intValue();
                v = m.get("collectionId");
                if (v instanceof Number) return ((Number) v).intValue();
            }
            // try getter getId()
            try {
                Method m = collection.getClass().getMethod("getId");
                Object val = m.invoke(collection);
                if (val instanceof Number) return ((Number) val).intValue();
            } catch (NoSuchMethodException ignored) {}

            // try getter getCollectionId()
            try {
                Method m = collection.getClass().getMethod("getCollectionId");
                Object val = m.invoke(collection);
                if (val instanceof Number) return ((Number) val).intValue();
            } catch (NoSuchMethodException ignored) {}

            // try fields named id or collectionId
            for (Field f : collection.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                if ("id".equals(f.getName()) || "collectionId".equals(f.getName())) {
                    Object val = f.get(collection);
                    if (val instanceof Number) return ((Number) val).intValue();
                }
            }
        } catch (Throwable t) {
            // swallow and return null
        }
        return null;
    }
}
