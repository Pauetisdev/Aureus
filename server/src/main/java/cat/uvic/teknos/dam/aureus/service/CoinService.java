package cat.uvic.teknos.dam.aureus.service;

import cat.uvic.teknos.dam.aureus.impl.CoinImpl;

import java.util.List;

/**
 * Service interface that defines operations for managing coins.
 *
 * <p>Implementations handle persistence and business rules related to
 * coin creation, retrieval, update and deletion. Methods use the
 * {@link cat.uvic.teknos.dam.aureus.impl.CoinImpl} concrete model used
 * across the application.</p>
 */
public interface CoinService {
    /**
     * Retrieve all coins available in the system.
     *
     * @return list of coins (may be empty)
     */
    List<CoinImpl> findAll();

    /**
     * Find a coin by its numeric identifier.
     *
     * @param id coin identifier
     * @return the coin instance
     */
    CoinImpl findById(int id);

    /**
     * Create a new coin. The implementation will persist the coin and
     * return the created instance with its generated id.
     *
     * @param coin coin data to create
     * @return created coin including generated id
     */
    CoinImpl create(CoinImpl coin);

    /**
     * Create a new coin and associate it with an existing collection.
     *
     * @param coin coin data to create
     * @param collectionId id of the collection to associate (may be null)
     * @return created coin including generated id and association
     */
    CoinImpl create(CoinImpl coin, Integer collectionId);

    /**
     * Update an existing coin. The provided coin object must contain a valid id.
     *
     * @param coin coin instance with updated fields
     */
    void update(CoinImpl coin);

    /**
     * Delete a coin by its id.
     *
     * @param id identifier of the coin to delete
     */
    void delete(int id);
}
