package cat.uvic.teknos.dam.aureus.repositories;

import java.util.List;

public interface Repository<K, V> {
    void save(V value);
    void delete(V value);
    V get(K id);
    List<V> getAll();
}