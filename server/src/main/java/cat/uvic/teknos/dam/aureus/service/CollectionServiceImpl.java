package cat.uvic.teknos.dam.aureus.service;

import cat.uvic.teknos.dam.aureus.impl.CollectionImpl;
import cat.uvic.teknos.dam.aureus.impl.CoinCollectionImpl;
import cat.uvic.teknos.dam.aureus.impl.UserImpl;
import cat.uvic.teknos.dam.aureus.Collection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory service that provides collection data for non-persistent scenarios.
 */
public class CollectionServiceImpl {
    private final List<CollectionImpl> store = new ArrayList<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    public CollectionServiceImpl() {
        // Seed some collections
        CollectionImpl c1 = new CollectionImpl();
        c1.setId(nextId.getAndIncrement());
        c1.setCollectionName("Imperial Antiquities");
        store.add(c1);

        CollectionImpl c2 = new CollectionImpl();
        c2.setId(nextId.getAndIncrement());
        c2.setCollectionName("Greek Hoard");
        store.add(c2);
    }

    /**
     * Return all collections (as the interface type) from the in-memory store.
     *
     * @return list of collections
     */
    public List<Collection> findAll() {
        return new ArrayList<>(store);
    }

    /**
     * Find a collection by id in the in-memory store.
     *
     * @param id collection id
     * @return collection or null if not found
     */
    public Collection findById(int id) {
        return store.stream().filter(c -> c.getId() != null && c.getId() == id).findFirst().orElse(null);
    }
}
