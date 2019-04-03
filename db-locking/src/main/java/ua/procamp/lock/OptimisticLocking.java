package ua.procamp.lock;

import lombok.extern.slf4j.Slf4j;
import ua.procamp.exception.DaoOperationException;
import ua.procamp.exception.OptimisticLockException;
import ua.procamp.exception.ResourseNotFoundException;
import ua.procamp.model.Programs;
import ua.procamp.util.JdbcUtil;

import javax.sql.DataSource;
import java.sql.*;

import static java.util.Optional.*;
import static ua.procamp.model.SqlQueries.*;

@Slf4j
public class OptimisticLocking {

    private DataSource dataSource;

    public void updateEntity(Programs programs) {

        ofNullable(programs)
                .orElseThrow(() -> new ResourseNotFoundException("Entity to update cannot be null"));

        initPostgresSqlDatasource();
        validateId(programs.getId());

        try (Connection connection = this.dataSource.getConnection()) {

            PreparedStatement preparedStatementVersion = connection.prepareStatement(FIND_ONE_SQL);
            preparedStatementVersion.setLong(1, programs.getId());
            ResultSet resultSet = preparedStatementVersion.executeQuery();

            Long currentVersion = getCurrentVersion(resultSet);

            PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_ROW_WITH_VERSION_CHECKING_SQL);
            preparedStatement.setString(1, programs.getName());
            preparedStatement.setLong(2, programs.getId());
            preparedStatement.setLong(3, ++currentVersion);
            preparedStatement.setLong(4, currentVersion);

            validateVersion(preparedStatement.executeUpdate());

        } catch (SQLException e) {
            throw new DaoOperationException("Error while processing update query: " + e.getMessage());
        }
    }

    private Long getCurrentVersion(ResultSet resultSet) {
        try {
            if (resultSet.next()) {
                long version = resultSet.getLong("version");
                log.info("current version is : {}", version);
                return version;
            } else {
                throw new ResourseNotFoundException("No rows found");
            }
        } catch (SQLException e) {
            throw new DaoOperationException("Error while processing update query: " + e.getMessage());
        }
    }

    private void validateVersion(int affectedRows) {
        if (affectedRows == 0) {
            throw new OptimisticLockException("Wrong version!");
        }
    }

    private void validateId(Long id) {
        if (id == null) {
            throw new DaoOperationException("Programs id cannot be null");
        } else if (id <= 0) {
            throw new DaoOperationException("Program with id = " + id + " does not exist");
        }
    }

    private void initPostgresSqlDatasource() {
        String url = String.format("jdbc:postgresql://localhost:5432/%s", "gl_db");
        this.dataSource = JdbcUtil.createPostgresDataSource(url, "gl_db", "global");
    }
}