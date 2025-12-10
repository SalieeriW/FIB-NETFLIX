package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    public static boolean validateUser(String username, String password) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseManager.getConnection();
            String query = "SELECT 1 FROM usuarios WHERE id_usuario = ? AND password = ?";
            statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, password);
            resultSet = statement.executeQuery();
            return resultSet.next();

        } catch (Exception e) {
            System.err.println("Error validating user: " + e.getMessage());
            return false;
        } finally {
            DatabaseManager.closeResultSet(resultSet);
            DatabaseManager.closeStatement(statement);
            DatabaseManager.closeConnection(connection);
        }
    }

    public static boolean userExists(String username) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseManager.getConnection();
            String query = "SELECT 1 FROM usuarios WHERE id_usuario = ?";
            statement = connection.prepareStatement(query);
            statement.setString(1, username);
            resultSet = statement.executeQuery();
            return resultSet.next();

        } catch (Exception e) {
            System.err.println("Error checking if user exists: " + e.getMessage());
            return false;
        } finally {
            DatabaseManager.closeResultSet(resultSet);
            DatabaseManager.closeStatement(statement);
            DatabaseManager.closeConnection(connection);
        }
    }

    public static boolean insertUser(String username, String password) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DatabaseManager.getConnection();
            String query = "INSERT INTO usuarios (id_usuario, password) VALUES (?, ?)";
            statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, password);
            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("Error inserting user: " + e.getMessage());
            return false;
        } finally {
            DatabaseManager.closeStatement(statement);
            DatabaseManager.closeConnection(connection);
        }
    }
}
