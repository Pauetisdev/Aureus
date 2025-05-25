package cat.uvic.teknos.dam.aureus.repositories.jdbc;

import cat.uvic.teknos.dam.aureus.UserDetail;
import cat.uvic.teknos.dam.aureus.impl.UserDetailImpl;
import cat.uvic.teknos.dam.aureus.repositories.UserDetailRepository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.DataSource;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.CrudException;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class JdbcUserDetailRepository implements UserDetailRepository {

    private final DataSource dataSource;

    public JdbcUserDetailRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(UserDetail userDetail) {
        var connection = dataSource.getConnection();

        try (var preparedStatement = connection.prepareStatement(
                "INSERT INTO USER_DETAIL (USER_ID, BIRTHDATE, PHONE, GENDER, NATIONALITY) VALUES (?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE BIRTHDATE = VALUES(BIRTHDATE), PHONE = VALUES(PHONE), GENDER = VALUES(GENDER), NATIONALITY = VALUES(NATIONALITY)"
        )) {
            preparedStatement.setInt(1, userDetail.getId());

            if (userDetail.getBirthday() != null) {
                preparedStatement.setDate(2, new java.sql.Date(userDetail.getBirthday().getTime()));
            } else {
                preparedStatement.setNull(2, java.sql.Types.DATE);
            }

            preparedStatement.setString(3, userDetail.getPhone());
            preparedStatement.setString(4, userDetail.getGender());
            preparedStatement.setString(5, userDetail.getNationality());

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new CrudException("Error saving UserDetail", e);
        }
    }

    @Override
    public void delete(UserDetail userDetail) {
        var connection = dataSource.getConnection();

        try (var preparedStatement = connection.prepareStatement(
                "DELETE FROM USER_DETAIL WHERE USER_ID = ?"
        )) {
            preparedStatement.setInt(1, userDetail.getId());
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new CrudException("Error deleting UserDetail", e);
        }
    }

    @Override
    public UserDetail get(Integer id) {
        var connection = dataSource.getConnection();

        try (var preparedStatement = connection.prepareStatement(
                "SELECT * FROM USER_DETAIL WHERE USER_ID = ?"
        )) {
            preparedStatement.setInt(1, id);
            var rs = preparedStatement.executeQuery();

            if (rs.next()) {
                var userDetail = new UserDetailImpl();
                userDetail.setId(rs.getInt("USER_ID"));

                var sqlDate = rs.getDate("BIRTHDATE");
                if (sqlDate != null) {
                    userDetail.setBirthday((java.sql.Date) new Date(sqlDate.getTime()));
                } else {
                    userDetail.setBirthday(null);
                }

                userDetail.setPhone(rs.getString("PHONE"));
                userDetail.setGender(rs.getString("GENDER"));
                userDetail.setNationality(rs.getString("NATIONALITY"));

                return userDetail;
            } else {
                return null;
            }

        } catch (SQLException e) {
            throw new CrudException("Error getting UserDetail", e);
        }
    }

    @Override
    public Set<UserDetail> getAll() {
        var connection = dataSource.getConnection();
        Set<UserDetail> userDetails = new HashSet<>();

        try (var preparedStatement = connection.prepareStatement(
                "SELECT * FROM USER_DETAIL"
        )) {
            var rs = preparedStatement.executeQuery();

            while (rs.next()) {
                var userDetail = new UserDetailImpl();
                userDetail.setId(rs.getInt("USER_ID"));

                var sqlDate = rs.getDate("BIRTHDATE");
                if (sqlDate != null) {
                    userDetail.setBirthday((java.sql.Date) new Date(sqlDate.getTime()));
                } else {
                    userDetail.setBirthday(null);
                }

                userDetail.setPhone(rs.getString("PHONE"));
                userDetail.setGender(rs.getString("GENDER"));
                userDetail.setNationality(rs.getString("NATIONALITY"));

                userDetails.add(userDetail);
            }

            return userDetails;

        } catch (SQLException e) {
            throw new CrudException("Error getting all UserDetails", e);
        }
    }
}
