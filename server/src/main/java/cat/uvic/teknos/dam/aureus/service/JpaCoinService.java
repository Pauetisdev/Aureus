package cat.uvic.teknos.dam.aureus.service;

import cat.uvic.teknos.dam.aureus.impl.CoinImpl;
import cat.uvic.teknos.dam.aureus.model.jpa.JpaCoin;
import cat.uvic.teknos.dam.aureus.model.jpa.repositories.JpaCoinRepository;
import cat.uvic.teknos.dam.aureus.service.exception.EntityNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

public class JpaCoinService implements CoinService {
    private final JpaCoinRepository coinRepository;

    public JpaCoinService(JpaCoinRepository coinRepository) {
        this.coinRepository = coinRepository;
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
        return jpaCoin;
    }
}

