package cat.uvic.teknos.dam.aureus.repositories.jdbc;

import cat.uvic.teknos.dam.aureus.UserDetail;
import cat.uvic.teknos.dam.aureus.impl.UserDetailImpl;
import cat.uvic.teknos.dam.aureus.repositories.UserDetailRepository;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.datasources.DataSource;
import cat.uvic.teknos.dam.aureus.repositories.jdbc.exceptions.CrudException;

import java.sql.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class JdbcUserDetailRepository implements UserDetailRepository {

    private final DataSource dataSource;

    public JdbcUserDetailRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(UserDetail userDetail) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO USER_DETAIL (USER_ID, BIRTHDATE, PHONE, GENDER, NATIONALITY) VALUES (?, ?, ?, ?, ?) " +
                             "ON DUPLICATE KEY UPDATE BIRTHDATE = VALUES(BIRTHDATE), PHONE = VALUES(PHONE), GENDER = VALUES(GENDER), NATIONALITY = VALUES(NATIONALITY)"
             )) {

            preparedStatement.setInt(1, userDetail.getId());

            LocalDate birthdate = userDetail.getBirthdate();
            if (birthdate != null) {
                preparedStatement.setDate(2, Date.valueOf(birthdate)); // LocalDate -> java.sql.Date
            } else {
                preparedStatement.setNull(2, Types.DATE);
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
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
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
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT * FROM USER_DETAIL WHERE USER_ID = ?"
             )) {

            preparedStatement.setInt(1, id);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    var result = new UserDetailImpl();
                    result.setId(rs.getInt("USER_ID"));

                    Date sqlDate = rs.getDate("BIRTHDATE");
                    if (sqlDate != null) {
                        result.setBirthdate(sqlDate.toLocalDate()); // ✅ java.sql.Date -> LocalDate
                    } else {
                        result.setBirthdate(null);
                    }

                    result.setPhone(rs.getString("PHONE"));
                    result.setGender(rs.getString("GENDER"));
                    result.setNationality(rs.getString("NATIONALITY"));

                    return result;
                } else {
                    return null;
                }
            }

        } catch (SQLException e) {
            throw new CrudException("Error getting UserDetail", e);
        }
    }

    @Override
    public Set<UserDetail> getAll() {
        Set<UserDetail> userDetails = new HashSet<>();

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT * FROM USER_DETAIL")) {

            while (rs.next()) {
                var userDetail = new UserDetailImpl();
                userDetail.setId(rs.getInt("USER_ID"));

                Date sqlDate = rs.getDate("BIRTHDATE");
                if (sqlDate != null) {
                    userDetail.setBirthdate(sqlDate.toLocalDate()); // ✅ Conversión correcta
                } else {
                    userDetail.setBirthdate(null);
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