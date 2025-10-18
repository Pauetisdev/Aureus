package cat.uvic.teknos.dam.aureus.service;

import cat.uvic.teknos.dam.aureus.impl.CoinImpl;

import java.util.List;

public interface CoinService {
    List<CoinImpl> findAll();
    CoinImpl findById(int id);
    CoinImpl create(CoinImpl coin);
    void update(CoinImpl coin);
    void delete(int id);
}
