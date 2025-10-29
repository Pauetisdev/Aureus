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

/**
 * REST-like controller handling coin-related HTTP operations.
 *
 * <p>This controller is responsible for receiving JSON payloads from the
 * client, normalizing different collection representations, delegating
 * persistence operations to a {@code CoinService}, and returning JSON
 * responses.</p>
 *
 * <p>Note: this class performs lenient parsing of collection identifiers
 * (supports both "collectionId" root fields and primitive/structured
 * "collection" values) to be tolerant with different client payloads.</p>
 */
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

    /**
     * Return all coins as a JSON array.
     *
     * @return JSON representation of the list of coins
     */
    public String getAllCoins() {
        List<CoinImpl> list = coinService.findAll();
        return gson.toJson(list);
    }

    /**
     * Return a single coin identified by its id as JSON.
     *
     * @param id numeric identifier of the coin to retrieve
     * @return JSON representation of the coin
     */
    public String getCoin(int id) {
        CoinImpl coin = coinService.findById(id);
        return gson.toJson(coin);
    }

    /**
     * Create a new coin from the provided JSON body.
     *
     * <p>The method accepts multiple forms for the collection association:
     * - a root-level "collectionId" integer field,
     * - a primitive "collection" value containing the id,
     * - or a structured "collection" object with an "id" property.
     * The controller normalizes these forms and delegates creation to the
     * configured {@code CoinService}.</p>
     *
     * @param body JSON request body describing the coin to create
     * @return JSON representation of the created coin (including generated id)
     * @throws RuntimeException any unexpected error raised during processing (propagated)
     */
    public String createCoin(String body) {
        // Normalizar la forma en que viene la colección: permitir collectionId en raíz o collection como primitivo
        try {
            com.google.gson.JsonElement parsed = com.google.gson.JsonParser.parseString(body);
            if (parsed.isJsonObject()) {
                com.google.gson.JsonObject obj = parsed.getAsJsonObject();
                boolean modified = false;
                // Si hay collectionId en la raíz, convertir a collection:{id:...}
                if (obj.has("collectionId") && !obj.get("collectionId").isJsonNull()) {
                    com.google.gson.JsonElement cid = obj.get("collectionId");
                    com.google.gson.JsonObject collObj = new com.google.gson.JsonObject();
                    collObj.add("id", cid);
                    obj.add("collection", collObj);
                    obj.remove("collectionId");
                    modified = true;
                } else if (obj.has("collection") && !obj.get("collection").isJsonNull()) {
                    com.google.gson.JsonElement collElem = obj.get("collection");
                    // Si collection es primitivo (número o string con número), convertir a objeto {id:...}
                    if (collElem.isJsonPrimitive()) {
                        com.google.gson.JsonPrimitive prim = collElem.getAsJsonPrimitive();
                        try {
                            com.google.gson.JsonObject collObj = new com.google.gson.JsonObject();
                            if (prim.isNumber()) {
                                collObj.addProperty("id", prim.getAsInt());
                            } else if (prim.isString()) {
                                collObj.addProperty("id", Integer.parseInt(prim.getAsString()));
                            }
                            obj.add("collection", collObj);
                            modified = true;
                        } catch (NumberFormatException ignored) {
                            // leave as-is
                        }
                    }
                }
                if (modified) {
                    body = obj.toString();
                }
            }
        } catch (Exception ignored) {}

        // --- LOG: imprimir body normalizado y extracción de collectionId antes de validar
        System.out.println("CoinController.createCoin: normalized body = " + body);
        Integer preExtractedCollectionId = null;
        try {
            com.google.gson.JsonElement parsedForLog = com.google.gson.JsonParser.parseString(body);
            if (parsedForLog.isJsonObject()) {
                com.google.gson.JsonObject objForLog = parsedForLog.getAsJsonObject();
                Integer cid = extractCollectionIdFromJsonObject(objForLog);
                preExtractedCollectionId = cid;
                System.out.println("CoinController.createCoin: extracted collectionId from JSON = " + cid);
                // Use the detected id for the rest of the flow
            }
        } catch (Exception ignored) {}

        // Fallback: intentar extraer con regex directamente del body (por si el parseo JSON falla por algún motivo)
        if (preExtractedCollectionId == null) {
            try {
                java.util.regex.Pattern p1 = java.util.regex.Pattern.compile("\"collectionId\"\s*:\s*(\\d+)");
                java.util.regex.Matcher m1 = p1.matcher(body);
                if (m1.find()) {
                    preExtractedCollectionId = Integer.parseInt(m1.group(1));
                    System.out.println("CoinController.createCoin: extracted collectionId from body via regex (collectionId) = " + preExtractedCollectionId);
                } else {
                    java.util.regex.Pattern p2 = java.util.regex.Pattern.compile("\"collection\"\s*:\s*\\{[^}]*\"id\"\s*:\s*(\\d+)[^}]*\\}");
                    java.util.regex.Matcher m2 = p2.matcher(body);
                    if (m2.find()) {
                        preExtractedCollectionId = Integer.parseInt(m2.group(1));
                        System.out.println("CoinController.createCoin: extracted collectionId from body via regex (collection.id) = " + preExtractedCollectionId);
                    }
                }
            } catch (Exception ignored) {}
        }

        // Validación a nivel de JSON para evitar problemas de deserialización parcial
        validateJsonForCreate(body, preExtractedCollectionId);
        // Deserializar el cuerpo en CoinImpl (sin depender de que collection se haya mapeado correctamente)
        CoinImpl coin = parsingGson.fromJson(body, CoinImpl.class);

        // LOG: imprimir estado de coin.collection tras deserialización
        System.out.println("CoinController.createCoin: coin.getCollection() after deserialization = " + coin.getCollection());

        // Si no detectamos collectionId antes, intentar ahora extraerlo desde la instancia deserializada
        if (preExtractedCollectionId == null) {
            try {
                Integer cidFromCoin = extractCollectionId(coin.getCollection());
                if (cidFromCoin != null) {
                    preExtractedCollectionId = cidFromCoin;
                    System.out.println("CoinController.createCoin: extracted collectionId from deserialized coin = " + cidFromCoin);
                }
            } catch (Throwable ignored) {}
        }

        // Si disponemos de collectionId detectado, delegar al servicio que cargará la colección con el repositorio/EM correcto
        if (preExtractedCollectionId != null) {
            System.out.println("CoinController.createCoin: delegating to coinService.create with collectionId=" + preExtractedCollectionId);
            // Ensure coin contains this collection id as well so convertToJpaCoin can pick it up
            try {
                if (coin.getCollection() == null) {
                    cat.uvic.teknos.dam.aureus.impl.CoinCollectionImpl ci = new cat.uvic.teknos.dam.aureus.impl.CoinCollectionImpl();
                    ci.setId(preExtractedCollectionId);
                    coin.setCollection(ci);
                } else {
                    // overwrite id to ensure consistency
                    try {
                        java.lang.reflect.Method m = coin.getCollection().getClass().getMethod("setId", Integer.class);
                        m.invoke(coin.getCollection(), preExtractedCollectionId);
                    } catch (Throwable ignored) {}
                }
            } catch (Throwable t) {
                // ignore
            }
            System.out.println("CoinController.createCoin: coinService implementation = " + coinService.getClass().getName());
            System.out.println("CoinController.createCoin: coin.getId() before create = " + coin.getId());
            try {
                CoinImpl created = coinService.create(coin, preExtractedCollectionId);
                return gson.toJson(created);
            } catch (Throwable t) {
                System.err.println("CoinController.createCoin: exception while creating coin: " + t.getMessage());
                t.printStackTrace(System.err);
                throw t;
            }
        }

        System.out.println("CoinController.createCoin: coinService implementation = " + coinService.getClass().getName());
        System.out.println("CoinController.createCoin: coin.getId() before create = " + coin.getId());
        try {
            CoinImpl created = coinService.create(coin);
            return gson.toJson(created);
        } catch (Throwable t) {
            System.err.println("CoinController.createCoin: exception while creating coin: " + t.getMessage());
            t.printStackTrace(System.err);
            throw t;
        }
    }

    // Nueva sobrecarga: acepta un collectionId ya extraído desde el JSON y lo considera válido
    private void validateJsonForCreate(String body, Integer preExtractedCollectionId) {
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

        // No exigimos collectionId aquí: la asignación/validación de la colección la realiza el servicio (por defecto o error claro)

        if (!missing.isEmpty()) {
            String msg = "Missing required fields: " + String.join(", ", missing);
            throw new HttpException(400, "Bad Request", msg);
        }
    }

    private boolean hasNonEmptyString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() && obj.get(key).isJsonPrimitive() && !obj.get(key).getAsString().trim().isEmpty();
    }

    // Nuevo: Actualiza usando el id de la ruta (sobrescribe cualquier id en el body)
    /**
     * Update an existing coin using the provided id and JSON body.
     *
     * <p>The method normalizes different collection representations and
     * enforces the path id as the coin identifier. It delegates to the
     * configured {@code CoinService} to perform the update.</p>
     *
     * @param id identifier of the coin to update (path parameter)
     * @param body JSON request body containing the updated coin fields
     */
    public void updateCoin(int id, String body) {
        // Normalize collection forms: accept collectionId in root or primitive collection
        try {
            com.google.gson.JsonElement parsed = com.google.gson.JsonParser.parseString(body);
            if (parsed.isJsonObject()) {
                com.google.gson.JsonObject obj = parsed.getAsJsonObject();
                boolean modified = false;
                if (obj.has("collectionId") && !obj.get("collectionId").isJsonNull()) {
                    com.google.gson.JsonElement cid = obj.get("collectionId");
                    com.google.gson.JsonObject collObj = new com.google.gson.JsonObject();
                    collObj.add("id", cid);
                    obj.add("collection", collObj);
                    obj.remove("collectionId");
                    modified = true;
                } else if (obj.has("collection") && !obj.get("collection").isJsonNull()) {
                    com.google.gson.JsonElement collElem = obj.get("collection");
                    // Si collection es primitivo (número o string con número), convertir a objeto {id:...}
                    if (collElem.isJsonPrimitive()) {
                        com.google.gson.JsonPrimitive prim = collElem.getAsJsonPrimitive();
                        try {
                            com.google.gson.JsonObject collObj = new com.google.gson.JsonObject();
                            if (prim.isNumber()) collObj.addProperty("id", prim.getAsInt());
                            else if (prim.isString()) collObj.addProperty("id", Integer.parseInt(prim.getAsString()));
                            obj.add("collection", collObj);
                            modified = true;
                        } catch (NumberFormatException ignored) {}
                    }
                }
                if (modified) body = obj.toString();
            }
        } catch (Exception ignored) {}

        // usar parsingGson para poder instanciar la implementación de CoinCollection si viene en el body
        System.out.println("CoinController.updateCoin: body = " + body);
        CoinImpl coin = parsingGson.fromJson(body, CoinImpl.class);
        coin.setId(id);
        System.out.println("CoinController.updateCoin: deserialized coin.collection = " + coin.getCollection());
        // If collectionId could be parsed from the raw JSON body, ensure coin.collection carries that id
        try {
            com.google.gson.JsonElement parsedForLog = com.google.gson.JsonParser.parseString(body);
            if (parsedForLog.isJsonObject()) {
                Integer cid = extractCollectionIdFromJsonObject(parsedForLog.getAsJsonObject());
                if (cid != null) {
                    try {
                        if (coin.getCollection() == null) {
                            cat.uvic.teknos.dam.aureus.impl.CoinCollectionImpl ci = new cat.uvic.teknos.dam.aureus.impl.CoinCollectionImpl();
                            ci.setId(cid);
                            coin.setCollection(ci);
                        } else {
                            try {
                                java.lang.reflect.Method m = coin.getCollection().getClass().getMethod("setId", Integer.class);
                                m.invoke(coin.getCollection(), cid);
                            } catch (Throwable ignored) {}
                        }
                        System.out.println("CoinController.updateCoin: enforced collectionId = " + cid);
                    } catch (Throwable ignored) {}
                }
            }
        } catch (Exception ignored) {}
        coinService.update(coin);
    }

    /**
     * Delete a coin by its identifier.
     *
     * @param id numeric identifier of the coin to delete
     */
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

    // Helper: extrae collectionId desde el JsonObject considerando varias formas (collectionId raiz, collection primitivo, collection.obj.id)
    private Integer extractCollectionIdFromJsonObject(com.google.gson.JsonObject obj) {
        try {
            if (obj.has("collectionId") && !obj.get("collectionId").isJsonNull()) {
                try { return obj.get("collectionId").getAsInt(); } catch (Exception e) { try { return Integer.parseInt(obj.get("collectionId").getAsString()); } catch (Exception ignored) {} }
            }
            if (obj.has("collection") && !obj.get("collection").isJsonNull()) {
                com.google.gson.JsonElement collElem = obj.get("collection");
                if (collElem.isJsonPrimitive()) {
                    try { return collElem.getAsInt(); } catch (Exception e) { try { return Integer.parseInt(collElem.getAsString()); } catch (Exception ignored) {} }
                } else if (collElem.isJsonObject()) {
                    com.google.gson.JsonObject collObj = collElem.getAsJsonObject();
                    if (collObj.has("id") && !collObj.get("id").isJsonNull()) {
                        try { return collObj.get("id").getAsInt(); } catch (Exception e) { try { return Integer.parseInt(collObj.get("id").getAsString()); } catch (Exception ignored) {} }
                    }
                    if (collObj.has("collectionId") && !collObj.get("collectionId").isJsonNull()) {
                        try { return collObj.get("collectionId").getAsInt(); } catch (Exception e) { try { return Integer.parseInt(collObj.get("collectionId").getAsString()); } catch (Exception ignored) {} }
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }
}
