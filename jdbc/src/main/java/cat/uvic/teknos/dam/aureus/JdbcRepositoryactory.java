package cat.uvic.teknos.dam.aureus;

import cat.uvic.teknos.dam.aureus.datasources.DataSource;
import cat.uvic.teknos.dam.aureus.datasources.SingleConnectionDataSource;
import cat.uvic.teknos.dam.aureus.repositories.RepositoryFactory;

public class JdbcRepositoryactory implements RepositoryFactory {
    private final DataSource dataSource;
    public JdbcRepositoryactory(){
        dataSource = new SingleConnectionDataSource();
    }
}
