package cat.uvic.teknos.dam.aureus.repositories;

import cat.uvic.teknos.dam.aureus.Coin;

import java.util.List;

public interface CoinRepository extends Repository<Integer, Coin> {
    List<Coin> findByMaterial(String material);
    List<Coin> findByYear(Integer year);
}
