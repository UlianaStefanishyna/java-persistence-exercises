package ua.procamp.dao;

@SuppressWarnings("all")
public class SqlQueries {

    public static final String SAVE_SQL = "INSERT INTO products (" +
            "name ," +
            "producer, " +
            "price, " +
            "expiration_date," +
            "creation_time) VALUES (?,?,?,?,?)";

    public static final String UPDATE_SQL = "UPDATE products SET name=?, producer=?, price=?,expiration_date=? WHERE id=?";
    public static final String REMOVE_SQL = "DELETE FROM products WHERE id=?";
    public static final String FIND_ALL_SQL = "SELECT * from products";
    public static final String FIND_ONE_SQL = "SELECT * FROM products WHERE id = ?";

}