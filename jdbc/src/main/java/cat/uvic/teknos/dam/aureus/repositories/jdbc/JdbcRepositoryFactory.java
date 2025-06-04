package cat.uvic.teknos.dam.aureus.repositories.jdbc;

import cat.uvic.teknos.dam.aureus.repositories.CoinRepository;
import cat.uvic.teknos.dam.aureus.repositories.CollectionRepository;
import cat.uvic.teknos.dam.aureus.repositories.TransactionRepository;
import cat.uvic.teknos.dam.aureus.repositories.UserRepository;
import cat.uvic.teknos.dam.aureus.repositories.UserDetailRepository;
import cat.uvic.teknos.dam.aureus.repositories.CoinCollectionRepository;
import cat.uvic.teknos.dam.aureus.repositories.CoinTransactionRepository;
import cat.uvic.teknos.dam.aureus.repositories.RepositoryFactory;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.DataSource;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.SingleConnectionDataSource;

public class JdbcRepositoryFactory implements RepositoryFactory {
    private final DataSource dataSource;
    private UserRepository userRepository;

    public JdbcRepositoryFactory() {
        this.dataSource = new SingleConnectionDataSource();
    }

    @Override
    public UserRepository getUserRepository() {
        return new JdbcUserRepository(dataSource);
    }

    @Override
    public UserDetailRepository getUserDetailRepository() {
        return new JdbcUserDetailRepository(dataSource);
    }

    @Override
    public CollectionRepository getCollectionRepository() {
        return new JdbcCollectionRepository(dataSource, userRepository);
    }

    @Override
    public CoinRepository getCoinRepository() {
        return new JdbcCoinRepository(dataSource);
    }

    @Override
    public TransactionRepository getTransactionRepository() {
        return new JdbcTransactionRepository(dataSource, userRepository);
    }


    @Override
    public CoinCollectionRepository getCoinCollectionRepository() {
        return new JdbcCoinCollectionRepository(dataSource);
    }

    @Override
    public CoinTransactionRepository getCoinTransactionRepository() {
        return new JdbcCoinTransactionRepository(dataSource);
    }
}
