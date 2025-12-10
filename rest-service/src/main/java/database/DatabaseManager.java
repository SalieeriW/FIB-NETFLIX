package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Database connection manager for Practica 5 REST service.
 * Uses Derby Network Server at localhost:1527 with database 'pr5'.
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:derby://localhost:1527/pr5;create=true";
    private static final String DB_DRIVER = "org.apache.derby.jdbc.ClientDriver";

    /**
     * Obtain a new database connection.
     */
    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName(DB_DRIVER);
        return DriverManager.getConnection(DB_URL);
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    public static void closeStatement(PreparedStatement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                System.err.println("Error closing statement: " + e.getMessage());
            }
        }
    }

    public static void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                System.err.println("Error closing result set: " + e.getMessage());
            }
        }
    }
}
