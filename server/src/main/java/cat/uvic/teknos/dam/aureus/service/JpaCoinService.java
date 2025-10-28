package cat.uvic.teknos.dam.aureus.service;

import cat.uvic.teknos.dam.aureus.impl.CoinImpl;
import cat.uvic.teknos.dam.aureus.model.jpa.JpaCoin;
import cat.uvic.teknos.dam.aureus.model.jpa.repositories.JpaCoinRepository;
import cat.uvic.teknos.dam.aureus.service.exception.EntityNotFoundException;
import cat.uvic.teknos.dam.aureus.http.exception.HttpException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JpaCoinService implements CoinService {
    private final JpaCoinRepository coinRepository;
    private final cat.uvic.teknos.dam.aureus.model.jpa.repositories.JpaCollectionRepository collectionRepository;

    public JpaCoinService(JpaCoinRepository coinRepository, cat.uvic.teknos.dam.aureus.model.jpa.repositories.JpaCollectionRepository collectionRepository) {
        this.coinRepository = coinRepository;
        this.collectionRepository = collectionRepository;
    }

    @Override
    public List<CoinImpl> findAll() {
        List<JpaCoin> jpaCoins = coinRepository.getAll();
        return jpaCoins.stream()
                .map(this::convertToCoinImpl)
                .collect(Collectors.toList());
    }

    @Override
    public CoinImpl findById(int id) {
        try {
            JpaCoin jpaCoin = coinRepository.get(id);
            return convertToCoinImpl(jpaCoin);
        } catch (Exception e) {
            throw new EntityNotFoundException("Coin not found with id " + id);
        }
    }

    @Override
    public CoinImpl create(CoinImpl coin) {
        JpaCoin jpaCoin = convertToJpaCoin(coin);
        coinRepository.save(jpaCoin);
        return convertToCoinImpl(jpaCoin);
    }

    @Override
    public void update(CoinImpl coin) {
        if (coin.getId() == null) {
            throw new IllegalArgumentException("Coin id is required for update");
        }
        JpaCoin jpaCoin = convertToJpaCoin(coin);
        coinRepository.save(jpaCoin);
    }

    @Override
    public void delete(int id) {
        try {
            JpaCoin jpaCoin = coinRepository.get(id);
            coinRepository.delete(jpaCoin);
        } catch (Exception e) {
            throw new EntityNotFoundException("Coin not found with id " + id);
        }
    }

    private CoinImpl convertToCoinImpl(JpaCoin jpaCoin) {
        CoinImpl coin = new CoinImpl();
        coin.setId(jpaCoin.getId());
        coin.setCoinName(jpaCoin.getCoinName());
        coin.setCoinYear(jpaCoin.getCoinYear());
        coin.setCoinMaterial(jpaCoin.getCoinMaterial());
        coin.setCoinWeight(jpaCoin.getCoinWeight());
        coin.setCoinDiameter(jpaCoin.getCoinDiameter());
        coin.setEstimatedValue(jpaCoin.getEstimatedValue());
        coin.setOriginCountry(jpaCoin.getOriginCountry());
        coin.setHistoricalSignificance(jpaCoin.getHistoricalSignificance());
        return coin;
    }

    private JpaCoin convertToJpaCoin(CoinImpl coin) {
        JpaCoin jpaCoin = new JpaCoin();
        if (coin.getId() != null) {
            jpaCoin.setId(coin.getId());
        }
        jpaCoin.setCoinName(coin.getCoinName());
        jpaCoin.setCoinYear(coin.getCoinYear());
        jpaCoin.setCoinMaterial(coin.getCoinMaterial());
        jpaCoin.setCoinWeight(coin.getCoinWeight());
        jpaCoin.setCoinDiameter(coin.getCoinDiameter());
        jpaCoin.setEstimatedValue(coin.getEstimatedValue());
        jpaCoin.setOriginCountry(coin.getOriginCountry());
        jpaCoin.setHistoricalSignificance(coin.getHistoricalSignificance());

        // Map collection if provided (extract id and load JpaCollection)
        Integer collectionId = extractCollectionId(coin.getCollection());
        if (collectionId != null) {
            try {
                var coll = collectionRepository.get(collectionId);
                // set to jpaCoin
                jpaCoin.setCollection(coll);
            } catch (cat.uvic.teknos.dam.aureus.service.exception.EntityNotFoundException e) {
                // explicit bad request when collection doesn't exist
                throw new HttpException(400, "Bad Request", "collectionId " + collectionId + " not found");
            } catch (Exception ignored) {
                // leave null, repository will throw on save if DB requires it
            }
        }
        return jpaCoin;
    }

    private Integer extractCollectionId(cat.uvic.teknos.dam.aureus.CoinCollection collection) {
        if (collection == null) return null;
        try {
            // If it's a Map-like implementation
            if (collection instanceof Map) {
                Map<?,?> m = (Map<?,?>) collection;
                Object v = m.get("id");
                if (v instanceof Number) return ((Number) v).intValue();
                v = m.get("collectionId");
                if (v instanceof Number) return ((Number) v).intValue();
            }
            // try getter getId()
            try {
                java.lang.reflect.Method m = collection.getClass().getMethod("getId");
                Object val = m.invoke(collection);
                if (val instanceof Number) return ((Number) val).intValue();
            } catch (NoSuchMethodException ignored) {}

            // try getter getCollectionId()
            try {
                java.lang.reflect.Method m = collection.getClass().getMethod("getCollectionId");
                Object val = m.invoke(collection);
                if (val instanceof Number) return ((Number) val).intValue();
            } catch (NoSuchMethodException ignored) {}

            // try fields named id or collectionId
            for (java.lang.reflect.Field f : collection.getClass().getDeclaredFields()) {
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
