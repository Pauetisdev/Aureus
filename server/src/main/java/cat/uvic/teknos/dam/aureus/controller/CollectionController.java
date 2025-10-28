package cat.uvic.teknos.dam.aureus.controller;

import cat.uvic.teknos.dam.aureus.model.jpa.JpaCollection;
import cat.uvic.teknos.dam.aureus.model.jpa.repositories.JpaCollectionRepository;
import com.google.gson.Gson;

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
}
