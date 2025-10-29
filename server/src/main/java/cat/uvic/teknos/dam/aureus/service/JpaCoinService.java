package cat.uvic.teknos.dam.aureus.service;

import cat.uvic.teknos.dam.aureus.impl.CoinImpl;
import cat.uvic.teknos.dam.aureus.model.jpa.JpaCoin;
import cat.uvic.teknos.dam.aureus.model.jpa.repositories.JpaCoinRepository;
import cat.uvic.teknos.dam.aureus.service.exception.EntityNotFoundException;
import cat.uvic.teknos.dam.aureus.http.exception.HttpException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JPA-backed implementation of {@link CoinService}.
 *
 * <p>This service converts between the application's {@code CoinImpl}
 * model and the JPA entity {@code JpaCoin}, performs validations and
 * delegates persistence to the {@link JpaCoinRepository} and
 * {@code JpaCollectionRepository}.</p>
 */
public class JpaCoinService implements CoinService {
    private final JpaCoinRepository coinRepository;
    private final cat.uvic.teknos.dam.aureus.model.jpa.repositories.JpaCollectionRepository collectionRepository;

    public JpaCoinService(JpaCoinRepository coinRepository, cat.uvic.teknos.dam.aureus.model.jpa.repositories.JpaCollectionRepository collectionRepository) {
        this.coinRepository = coinRepository;
        this.collectionRepository = collectionRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CoinImpl> findAll() {
        List<JpaCoin> jpaCoins = coinRepository.getAll();
        return jpaCoins.stream()
                .map(this::convertToCoinImpl)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CoinImpl findById(int id) {
        try {
            JpaCoin jpaCoin = coinRepository.get(id);
            return convertToCoinImpl(jpaCoin);
        } catch (Exception e) {
            throw new EntityNotFoundException("Coin not found with id " + id);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CoinImpl create(CoinImpl coin) {
        System.out.println("JpaCoinService.create: incoming coin payload -> " + coin);
        // Check DB schema presence early
        try {
            if (!coinRepository.isSchemaPresent()) {
                throw new HttpException(500, "Internal Server Error", "Database schema missing: COIN table not found");
            }
        } catch (HttpException he) {
            throw he;
        } catch (Exception e) {
            System.err.println("JpaCoinService.create: error checking schema: " + e.getMessage());
            e.printStackTrace(System.err);
            throw new HttpException(500, "Internal Server Error", "Error checking database schema: " + e.getMessage());
        }

        JpaCoin jpaCoin = convertToJpaCoin(coin);

        // If conversion didn't set collection but the original coin contained a collection id, try to use it
        if (jpaCoin.getCollection() == null) {
            try {
                Integer direct = extractCollectionId(coin.getCollection());
                System.out.println("JpaCoinService.create: detect collectionId from original coin -> " + direct);
                if (direct != null) {
                    try {
                        var collFromCoin = collectionRepository.get(direct);
                        jpaCoin.setCollection(collFromCoin);
                        System.out.println("JpaCoinService.create: assigned collection from coin.collection id=" + collFromCoin.getId());
                    } catch (Exception e) {
                        System.err.println("JpaCoinService.create: collection id from coin not found: " + direct);
                    }
                }
            } catch (Throwable t) {
                // continue to fallback
            }
        }

        // Si no se proporcionó collection, intentar asignar una por defecto para evitar 400 y facilitar pruebas
        if (jpaCoin.getCollection() == null) {
            try {
                System.out.println("JpaCoinService.create: no collection provided, attempting to assign default collection id=1");
                try {
                    var defaultColl = collectionRepository.get(1);
                    jpaCoin.setCollection(defaultColl);
                    System.out.println("JpaCoinService.create: assigned default collection id=1");
                } catch (Exception e) {
                    // id=1 not found, fallback to first available
                    var all = collectionRepository.getAll();
                    if (!all.isEmpty()) {
                        jpaCoin.setCollection(all.get(0));
                        System.out.println("JpaCoinService.create: assigned first available collection id=" + all.get(0).getId());
                    } else {
                        System.out.println("JpaCoinService.create: no collections available to assign");
                    }
                }
            } catch (Exception e) {
                System.err.println("JpaCoinService.create: error while attempting to assign default collection: " + e.getMessage());
            }
        }

        // Validate required JPA fields before attempting to save to provide clear 4xx responses
        java.util.List<String> missing = new java.util.ArrayList<>();
        if (jpaCoin.getCoinName() == null || jpaCoin.getCoinName().trim().isEmpty()) missing.add("coinName");
        if (jpaCoin.getCoinMaterial() == null || jpaCoin.getCoinMaterial().trim().isEmpty()) missing.add("coinMaterial");
        if (jpaCoin.getEstimatedValue() == null) missing.add("estimatedValue");
        if (jpaCoin.getOriginCountry() == null || jpaCoin.getOriginCountry().trim().isEmpty()) missing.add("originCountry");
        if (jpaCoin.getCollection() == null) missing.add("collectionId (collection not found or not provided)");
        if (!missing.isEmpty()) {
            String msg = "Missing or invalid required fields: " + String.join(", ", missing);
            throw new HttpException(400, "Bad Request", msg);
        }

        try {
            coinRepository.save(jpaCoin);
        } catch (Exception e) {
            System.err.println("JpaCoinService.create: error saving JPA coin: " + e.getMessage());
            e.printStackTrace(System.err);
            // Unwrap root cause to give a clearer message back to the client
            Throwable root = e;
            while (root.getCause() != null) root = root.getCause();
            String rootMsg = (root.getMessage() != null) ? root.getMessage() : root.getClass().getSimpleName();
            String msg = root.getClass().getSimpleName() + ": " + rootMsg;
            throw new HttpException(500, "Internal Server Error", msg);
        }
        return convertToCoinImpl(jpaCoin);
    }


    @Override
    public CoinImpl create(CoinImpl coin, Integer collectionId) {
        if (collectionId == null) return create(coin);

        System.out.println("JpaCoinService.create(coin, collectionId): requested collectionId=" + collectionId);
        // comprobar esquema
        try {
            if (!coinRepository.isSchemaPresent()) {
                throw new HttpException(500, "Internal Server Error", "Database schema missing: COIN table not found");
            }
        } catch (HttpException he) {
            throw he;
        } catch (Exception e) {
            System.err.println("JpaCoinService.create(coin,collectionId): error checking schema: " + e.getMessage());
            throw new HttpException(500, "Internal Server Error", "Error checking database schema: " + e.getMessage());
        }

        // convert coin -> jpaCoin
        JpaCoin jpaCoin = convertToJpaCoin(coin);

        // cargar y fijar la colección solicitada - si no existe lanzar 400
        try {
            var coll = collectionRepository.get(collectionId);
            System.out.println("JpaCoinService.create(coin, collectionId): loaded collection -> id=" + coll.getId() + ", name='" + coll.getCollectionName() + "'");
            System.out.println("JpaCoinService.create(coin, collectionId): jpaCoin.collection before set = " + jpaCoin.getCollection());
            // Use a lightweight reference to ensure the FK is set correctly when persisting
            try {
                cat.uvic.teknos.dam.aureus.model.jpa.JpaCollection lightweight = new cat.uvic.teknos.dam.aureus.model.jpa.JpaCollection();
                lightweight.setId(coll.getId());
                jpaCoin.setCollection(lightweight);
                System.out.println("JpaCoinService.create(coin, collectionId): assigned lightweight JpaCollection id=" + lightweight.getId());
            } catch (Throwable t) {
                // fallback: set the loaded collection directly
                jpaCoin.setCollection(coll);
            }
            System.out.println("JpaCoinService.create(coin, collectionId): jpaCoin.collection after set = " + (jpaCoin.getCollection() == null ? "null" : jpaCoin.getCollection().getId()));
            System.out.println("JpaCoinService.create(coin, collectionId): will persist with collection id=" + coll.getId());
        } catch (Exception e) {
            System.err.println("JpaCoinService.create(coin, collectionId): collection not found id=" + collectionId);
            throw new HttpException(400, "Bad Request", "collectionId " + collectionId + " not found");
        }

        // validar campos requeridos antes de persistir
        java.util.List<String> missing = new java.util.ArrayList<>();
        if (jpaCoin.getCoinName() == null || jpaCoin.getCoinName().trim().isEmpty()) missing.add("coinName");
        if (jpaCoin.getCoinMaterial() == null || jpaCoin.getCoinMaterial().trim().isEmpty()) missing.add("coinMaterial");
        if (jpaCoin.getEstimatedValue() == null) missing.add("estimatedValue");
        if (jpaCoin.getOriginCountry() == null || jpaCoin.getOriginCountry().trim().isEmpty()) missing.add("originCountry");
        if (jpaCoin.getCollection() == null) missing.add("collectionId (collection not found or not provided)");
        if (!missing.isEmpty()) {
            String msg = "Missing or invalid required fields: " + String.join(", ", missing);
            throw new HttpException(400, "Bad Request", msg);
        }

        try {
            coinRepository.save(jpaCoin);
        } catch (Exception e) {
            System.err.println("JpaCoinService.create(coin, collectionId): error saving JPA coin: " + e.getMessage());
            Throwable root = e;
            while (root.getCause() != null) root = root.getCause();
            String rootMsg = (root.getMessage() != null) ? root.getMessage() : root.getClass().getSimpleName();
            throw new HttpException(500, "Internal Server Error", root.getClass().getSimpleName() + ": " + rootMsg);
        }
        return convertToCoinImpl(jpaCoin);
    }

    @Override
    public void update(CoinImpl coin) {
        if (coin.getId() == null) {
            throw new IllegalArgumentException("Coin id is required for update");
        }
        System.out.println("JpaCoinService.update: updating coin id=" + coin.getId());

        // Check DB schema presence early
        try {
            if (!coinRepository.isSchemaPresent()) {
                throw new HttpException(500, "Internal Server Error", "Database schema missing: COIN table not found");
            }
        } catch (HttpException he) {
            throw he;
        } catch (Exception e) {
            System.err.println("JpaCoinService.update: error checking schema: " + e.getMessage());
            e.printStackTrace(System.err);
            throw new HttpException(500, "Internal Server Error", "Error checking database schema: " + e.getMessage());
        }

        // Load existing JPA coin to update in-place (avoids transient/reference problems)
        JpaCoin existing;
        try {
            existing = coinRepository.get(coin.getId());
            }
        catch (Exception e) {
            throw new EntityNotFoundException("Coin not found with id " + coin.getId());
        }

        // Update scalar fields
        if (coin.getCoinName() != null) existing.setCoinName(coin.getCoinName());
        existing.setCoinYear(coin.getCoinYear());
        if (coin.getCoinMaterial() != null) existing.setCoinMaterial(coin.getCoinMaterial());
        existing.setCoinWeight(coin.getCoinWeight());
        existing.setCoinDiameter(coin.getCoinDiameter());
        existing.setEstimatedValue(coin.getEstimatedValue());
        if (coin.getOriginCountry() != null) existing.setOriginCountry(coin.getOriginCountry());
        existing.setHistoricalSignificance(coin.getHistoricalSignificance());

        // If the incoming CoinImpl carried a collection id, load that collection and assign it to the managed entity
        Integer collId = extractCollectionId(coin.getCollection());
        if (collId != null) {
            try {
                var coll = collectionRepository.get(collId);
                System.out.println("JpaCoinService.update: setting collection id=" + coll.getId() + " on existing coin");
                // Assign the managed collection entity directly to avoid persistence-context mismatch
                existing.setCollection(coll);
                System.out.println("JpaCoinService.update: assigned managed JpaCollection id=" + coll.getId());
            } catch (Exception e) {
                System.err.println("JpaCoinService.update: collection not found id=" + collId);
                throw new HttpException(400, "Bad Request", "collectionId " + collId + " not found");
            }
        }

        // Persist changes
        try {
            System.out.println("JpaCoinService.update: existing coin collection before save = " + (existing.getCollection() == null ? "null" : existing.getCollection().getId()));
            coinRepository.save(existing);
            // reload and log persisted collection id (sanity check)
            try {
                JpaCoin reloaded = coinRepository.get(existing.getId());
                System.out.println("JpaCoinService.update: reloaded coin collection after save = " + (reloaded.getCollection() == null ? "null" : reloaded.getCollection().getId()));
            } catch (Throwable ignored) {}
        } catch (Exception e) {
            System.err.println("JpaCoinService.update: error saving JPA coin: " + e.getMessage());
            e.printStackTrace(System.err);
            Throwable root = e;
            while (root.getCause() != null) root = root.getCause();
            String rootMsg = (root.getMessage() != null) ? root.getMessage() : root.getClass().getSimpleName();
            throw new HttpException(500, "Internal Server Error", root.getClass().getSimpleName() + ": " + rootMsg);
        }
    }

    @Override
    public void delete(int id) {
        try {
            JpaCoin jpaCoin = coinRepository.get(id);
            coinRepository.delete(jpaCoin);
        } catch (Exception e) {
            System.err.println("JpaCoinService.delete: error deleting coin id=" + id + " - " + e.getMessage());
            e.printStackTrace(System.err);
            throw new HttpException(500, "Internal Server Error", "Error deleting coin: " + e.getMessage());
        }
    }

    private CoinImpl convertToCoinImpl(JpaCoin jpaCoin) {
        if (jpaCoin == null) return null;
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
        // Map collection if present
        try {
            if (jpaCoin.getCollection() != null) {
                cat.uvic.teknos.dam.aureus.impl.CoinCollectionImpl ci = new cat.uvic.teknos.dam.aureus.impl.CoinCollectionImpl();
                ci.setId(jpaCoin.getCollection().getId());
                try {
                    String name = jpaCoin.getCollection().getCollectionName();
                    if (name != null) {
                        cat.uvic.teknos.dam.aureus.impl.CollectionImpl collImpl = new cat.uvic.teknos.dam.aureus.impl.CollectionImpl();
                        collImpl.setId(jpaCoin.getCollection().getId());
                        collImpl.setCollectionName(name);
                        ci.setCollection(collImpl);
                    }
                } catch (Throwable ignored) {}
                coin.setCollection(ci);
            }
        } catch (Throwable ignored) {}
        return coin;
    }

    private JpaCoin convertToJpaCoin(CoinImpl coin) {
        if (coin == null) return null;
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
        // collection se establece más adelante, si es necesario
        return jpaCoin;
    }

    private Integer extractCollectionId(Object collectionObj) {
        if (collectionObj == null) return null;
        try {
            // If it's a Number (Integer, Long, Double...) return int value
            if (collectionObj instanceof Number) {
                return ((Number) collectionObj).intValue();
            }

            // If it's a Map (e.g., Gson LinkedTreeMap), try common keys
            if (collectionObj instanceof Map) {
                Map<?, ?> m = (Map<?, ?>) collectionObj;
                Object v = m.get("id");
                if (v instanceof Number) return ((Number) v).intValue();
                v = m.get("collectionId");
                if (v instanceof Number) return ((Number) v).intValue();
                // sometimes strings
                v = m.get("id");
                if (v instanceof String) {
                    try { return Integer.parseInt((String) v); } catch (Exception ignored) {}
                }
            }

            // Try reflective getters: getId(), getCollectionId()
            try {
                java.lang.reflect.Method m = collectionObj.getClass().getMethod("getId");
                Object val = m.invoke(collectionObj);
                if (val instanceof Number) return ((Number) val).intValue();
            } catch (NoSuchMethodException ignored) {}

            try {
                java.lang.reflect.Method m2 = collectionObj.getClass().getMethod("getCollectionId");
                Object val2 = m2.invoke(collectionObj);
                if (val2 instanceof Number) return ((Number) val2).intValue();
            } catch (NoSuchMethodException ignored) {}

            // Try fields named id or collectionId
            for (java.lang.reflect.Field f : collectionObj.getClass().getDeclaredFields()) {
                try {
                    f.setAccessible(true);
                    if ("id".equals(f.getName()) || "collectionId".equals(f.getName())) {
                        Object val = f.get(collectionObj);
                        if (val instanceof Number) return ((Number) val).intValue();
                        if (val instanceof String) {
                            try { return Integer.parseInt((String) val); } catch (Exception ignored) {}
                        }
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Throwable t) {
            // swallow and return null
        }
        return null;
    }

}
