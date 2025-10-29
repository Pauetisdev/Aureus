package cat.uvic.teknos.dam.aureus.controller;

import cat.uvic.teknos.dam.aureus.model.jpa.JpaCollection;
import cat.uvic.teknos.dam.aureus.model.jpa.repositories.JpaCollectionRepository;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.List;
import java.util.stream.Collectors;

public class CollectionController {
    private final JpaCollectionRepository service;
    private final Gson gson = new Gson();

    public CollectionController(JpaCollectionRepository service) {
        this.service = service;
    }

    public String getAllCollections() {
        List<JpaCollection> list = service.getAll();
        // Return only id and collectionName to the client
        List<java.util.Map<String,Object>> reduced = list.stream()
                .map(c -> {
                    java.util.Map<String,Object> m = new java.util.HashMap<>();
                    m.put("id", c.getId());
                    m.put("collectionName", c.getCollectionName());
                    return m;
                })
                .collect(Collectors.toList());
        return gson.toJson(reduced);
    }

    // Nuevo: crear una colección a partir de JSON { "collectionName": "name", "description": "..." }
    public String createCollection(String body) {
        JsonElement parsed = JsonParser.parseString(body);
        if (!parsed.isJsonObject()) {
            throw new IllegalArgumentException("Request body must be a JSON object");
        }
        JsonObject obj = parsed.getAsJsonObject();
        if (!obj.has("collectionName") || obj.get("collectionName").isJsonNull() || obj.get("collectionName").getAsString().trim().isEmpty()) {
            throw new IllegalArgumentException("collectionName is required");
        }

        String name = obj.get("collectionName").getAsString().trim();
        String description = null;
        if (obj.has("description") && !obj.get("description").isJsonNull()) {
            description = obj.get("description").getAsString();
        }

        JpaCollection coll = new JpaCollection();
        coll.setCollectionName(name);
        coll.setDescription(description);

        // Persist using repository
        service.save(coll);

        // Return created object (id + name + description)
        java.util.Map<String,Object> resp = new java.util.HashMap<>();
        resp.put("id", coll.getId());
        resp.put("collectionName", coll.getCollectionName());
        resp.put("description", coll.getDescription());
        return gson.toJson(resp);
    }
}
