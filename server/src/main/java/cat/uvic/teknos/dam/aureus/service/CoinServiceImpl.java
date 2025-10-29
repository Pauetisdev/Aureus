package cat.uvic.teknos.dam.aureus.service;


import cat.uvic.teknos.dam.aureus.impl.CoinImpl;
import cat.uvic.teknos.dam.aureus.service.exception.EntityNotFoundException;
import cat.uvic.teknos.dam.aureus.impl.CoinCollectionImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CoinServiceImpl implements CoinService {

    private final Map<Integer, CoinImpl> store = new ConcurrentHashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    public CoinServiceImpl() {
        // Seed data
        CoinImpl c1 = new CoinImpl();
        c1.setId(nextId.getAndIncrement());
        c1.setCoinName("Denarius");
        c1.setCoinYear(50);
        store.put(c1.getId(), c1);

        CoinImpl c2 = new CoinImpl();
        c2.setId(nextId.getAndIncrement());
        c2.setCoinName("Drachma");
        c2.setCoinYear(-300);
        store.put(c2.getId(), c2);
    }

    @Override
    public List<CoinImpl> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public CoinImpl findById(int id) {
        CoinImpl c = store.get(id);
        if (c == null) throw new EntityNotFoundException("Coin not found with id " + id);
        return c;
    }

    @Override
    public CoinImpl create(CoinImpl coin) {
        if (coin.getId() == null) {
            coin.setId(nextId.getAndIncrement());
        } else {
            nextId.updateAndGet(curr -> Math.max(curr, coin.getId() + 1));
        }
        store.put(coin.getId(), coin);
        return coin;
    }

    @Override
    public CoinImpl create(CoinImpl coin, Integer collectionId) {
        if (collectionId != null) {
            // Si no tiene colección, crear una implementación mínima con el id
            if (coin.getCollection() == null) {
                CoinCollectionImpl ci = new CoinCollectionImpl();
                ci.setId(collectionId);
                coin.setCollection(ci);
            } else {
                // si ya tiene una colección, intentar fijar el id si es posible
                try {
                    java.lang.reflect.Method m = coin.getCollection().getClass().getMethod("setId", Integer.class);
                    m.invoke(coin.getCollection(), collectionId);
                } catch (Throwable ignored) {
                    // no hacemos nada si no se puede setear
                }
            }
        }
        return create(coin);
    }

    @Override
    public void update(CoinImpl coin) {
        if (coin.getId() == null) throw new IllegalArgumentException("Coin id is required for update");
        if (!store.containsKey(coin.getId())) throw new EntityNotFoundException("Coin not found with id " + coin.getId());
        store.put(coin.getId(), coin);
    }

    @Override
    public void delete(int id) {
        if (store.remove(id) == null) throw new EntityNotFoundException("Coin not found with id " + id);
    }
}
