package cat.uvic.teknos.dam.aureus.repositories;

import cat.uvic.teknos.dam.aureus.CoinCollection;

import java.util.List;

public interface CoinCollectionRepository extends Repository<Integer, CoinCollection> {
    List<CoinCollection> findByCollectionId(Integer collectionId);
    List<CoinCollection> findByCoinId(Integer coinId);
}
