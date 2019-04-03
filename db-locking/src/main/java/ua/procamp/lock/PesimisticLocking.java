package ua.procamp.lock;

import lombok.extern.slf4j.Slf4j;
import ua.procamp.exception.DaoOperationException;
import ua.procamp.exception.ResourseNotFoundException;
import ua.procamp.model.Programs;
import ua.procamp.util.JdbcUtil;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static java.util.Optional.ofNullable;
import static ua.procamp.model.SqlQueries.*;

@Slf4j
public class PesimisticLocking {

    private DataSource dataSource;

    public void updateEntity(Programs programs) {

        ofNullable(programs)
                .orElseThrow(() -> new ResourseNotFoundException("Entity to update cannot be null"));

        initPostgresSqlDatasource();
        validateId(programs.getId());

        try (Connection connection = this.dataSource.getConnection()) {

            connection.setAutoCommit(false);
            PreparedStatement preparedStatementToFindRow = connection.prepareStatement(FIND_ONE_WITH_SHARE_LOCK_SQL);
            preparedStatementToFindRow.setLong(1, programs.getId());

            // or just execute() without returning resultSet (just for locking)
            ResultSet resultSetBeforeUpdate = preparedStatementToFindRow.executeQuery();
            Programs program = getRow(resultSetBeforeUpdate);
            log.info("program before update = {}", program);

            PreparedStatement preparedStatementToUpdate = connection.prepareStatement(UPDATE_ROW_SQL);
            preparedStatementToUpdate.setString(1, programs.getName());
            preparedStatementToUpdate.setLong(2, programs.getId());
            preparedStatementToUpdate.execute();
            connection.commit();

            ResultSet resultSetAfterUpdate = preparedStatementToFindRow.executeQuery();
            Programs updatedProgram = getRow(resultSetAfterUpdate);
            log.info("updated program = {}", updatedProgram);

        } catch (SQLException e) {
            throw new DaoOperationException("Error while processing update query: " + e.getMessage());
        }
    }


    private Programs getRow(ResultSet resultSet) {
        try {
            if (resultSet.next()) {
                return Programs.builder()
                        .id(resultSet.getLong("id"))
                        .description(resultSet.getString("description"))
                        .name(resultSet.getString("name"))
                        .version(resultSet.getLong("version")).build();
            } else {
                throw new DaoOperationException("Error while parsing result set");
            }
        } catch (SQLException e) {
            throw new DaoOperationException("Error while parsing result set");
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