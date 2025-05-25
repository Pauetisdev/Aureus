package cat.uvic.teknos.dam.aureus.repositories;

public interface RepositoryFactory {
    CoinRepository getCoinRepository();
    CollectionRepository getCollectionRepository();
    TransactionRepository getTransactionRepository();
    UserRepository getUserRepository();
    UserDetailRepository getUserDetailRepository();

    CoinCollectionRepository getCoinCollectionRepository();
    CoinTransactionRepository getCoinTransactionRepository();
}