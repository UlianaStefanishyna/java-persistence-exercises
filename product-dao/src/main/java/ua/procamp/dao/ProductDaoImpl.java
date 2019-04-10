package ua.procamp.dao;

import ua.procamp.exception.DaoOperationException;
import ua.procamp.model.Product;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.*;
import static ua.procamp.dao.SqlQueries.*;

public class ProductDaoImpl implements ProductDao {
    private DataSource dataSource;

    public ProductDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(Product product) {
        handleSaveOperation(product, SAVE_SQL);
    }

    @Override
    public List<Product> findAll() {
        return handleOperationsWithReturningResult(FIND_ALL_SQL);
    }

    @Override
    public Product findOne(Long id) {
        validateId(id);
        return handleFindProductById(id, FIND_ONE_SQL);
    }

    private Product handleFindProductById(Long id, String sqlQuery) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            return createResult(resultSet).stream().findAny().orElseGet(Product::new);
        } catch (SQLException e) {
            throw new DaoOperationException("Product with id=" + id + " is not found");
        }
    }

    @Override
    public void update(Product product) {
        validateId(product.getId());
        handleUpdateOperation(product, UPDATE_SQL);
    }

    @Override
    public void remove(Product product) {
        validateId(product.getId());
        handleRemoveOperation(product, REMOVE_SQL);
    }

    private void setParamsToPreparedStatement(PreparedStatement preparedStatement, Product product) throws SQLException {
        preparedStatement.setString(1, product.getName());
        preparedStatement.setString(2, product.getProducer());
        preparedStatement.setBigDecimal(3, product.getPrice());
        preparedStatement.setDate(4, Date.valueOf(product.getExpirationDate()));
    }

    private List<Product> createResult(ResultSet resultSet) throws SQLException {

        List<Product> resultList = new ArrayList<>();

        while (resultSet.next()) {
            resultList.add(Product.builder()
                    .id(resultSet.getLong("id"))
                    .name(resultSet.getString("name"))
                    .producer(resultSet.getString("producer"))
                    .price(resultSet.getBigDecimal("price"))
                    .expirationDate(resultSet.getDate("expiration_date").toLocalDate())
                    .creationTime(resultSet.getTimestamp("creation_time").toLocalDateTime()).build());
        }
        return resultList;
    }

    private void handleRemoveOperation(Product product, String sqlQuery) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
            preparedStatement.setLong(1, product.getId());
            validateAffectedRows(preparedStatement.executeUpdate());
        } catch (SQLException e) {
            throw new DaoOperationException("Error removing the product: " + product);
        }
    }

    private void handleUpdateOperation(Product product, String sqlQuery) {
        try (Connection connection = this.dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
            setParamsToPreparedStatement(preparedStatement, product);
            preparedStatement.setLong(5, product.getId());

            validateAffectedRows(preparedStatement.executeUpdate());
        } catch (SQLException e) {
            throw new DaoOperationException("Error updating the product: " + product);
        }
    }

    private void handleSaveOperation(Product product, String sqlQuery) {
        try (Connection connection = this.dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection
                    .prepareStatement(sqlQuery, PreparedStatement.RETURN_GENERATED_KEYS);
            setParamsToPreparedStatement(preparedStatement, product);

            Timestamp timestamp = createTimestamp(product.getCreationTime());
            preparedStatement.setTimestamp(5, timestamp);

            validateAffectedRows(preparedStatement.executeUpdate());

            Long generatedId = getGeneratedId(preparedStatement);
            product.setId(generatedId);
        } catch (SQLException e) {
            throw new DaoOperationException("Error saving product: " + product);
        }
    }

    private Timestamp createTimestamp(LocalDateTime localDateTime) {
        return ofNullable(localDateTime)
                .map(Timestamp::valueOf)
                .orElseGet(() -> Timestamp.valueOf(LocalDateTime.now()));
    }

    private List<Product> handleOperationsWithReturningResult(String sqlQuery) {
        try (Connection connection = this.dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
            ResultSet resultSet = preparedStatement.executeQuery();
            return createResult(resultSet);
        } catch (SQLException e) {
            throw new DaoOperationException("Sql exception occurred");
        }
    }

    private void validateId(Long id) {
        if (id == null) {
            throw new DaoOperationException("Product id cannot be null");
        } else if (id <= 0) {
            throw new DaoOperationException("Product with id = " + id + " does not exist");
        }
    }

    private void validateAffectedRows(int count) {
        if (count == 0) {
            throw new DaoOperationException("No rows affected");
        }
    }

    private Long getGeneratedId(PreparedStatement preparedStatement) {
        try {
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getLong(1);
            } else {
                throw new DaoOperationException("No id found");
            }
        } catch (SQLException e) {
            throw new DaoOperationException("No id found");
        }
    }
}