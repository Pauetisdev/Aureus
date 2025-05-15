package cat.uvic.teknos.dam.aureus;

public interface AureusRepository {
    void insert(Coin coin);
    void update(Coin coin);
    void delete(Coin coin);
}
