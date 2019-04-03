package ua.procamp.model;

public class SqlQueries {

    public static final String FIND_ONE_SQL = "SELECT * from programs WHERE id=?";
    public static final String UPDATE_ROW_WITH_VERSION_CHECKING_SQL = "UPDATE programs SET (name,version) =(?,?) WHERE id=? and version=?";

    public static final String FIND_ONE_WITH_SHARE_LOCK_SQL = "SELECT * from programs WHERE id=? FOR UPDATE ";
    public static final String UPDATE_ROW_SQL = "UPDATE programs SET name=? WHERE id=?";
}