package cat.uvic.teknos.dam.aureus;

import java.util.Set;

public interface Repository<K, V> {
    void save(V value);
    void delete(V value);
    V get(K id);
    Set<V> getAll();
}
