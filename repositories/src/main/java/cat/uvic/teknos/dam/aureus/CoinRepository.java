package cat.uvic.teknos.dam.aureus;

import cat.uvic.teknos.dam.aureus.model.Coin;

import java.util.Set;

public interface CoinRepository {
    void insert(Coin coin);
    void update(Coin coin);
    void delete(Coin coin);
    Coin getById(Integer coinId);
    Set<Coin> getAll();
}
