package servlet;

import database.DatabaseManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Convenience servlet to initialise the practicum database.
 * Access: http://localhost:8080/practica5-rest-service/init-database
 */
@WebServlet("/init-database")
public class InitDatabaseServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html><html><head><title>Database Initialisation</title>");
            out.println("<style>body{font-family:Arial;margin:40px auto;max-width:640px;} .ok{padding:16px;background:#d4edda;color:#155724;border-radius:6px;} .error{padding:16px;background:#f8d7da;color:#721c24;border-radius:6px;} pre{padding:12px;background:#1f2933;color:#f9fafb;border-radius:6px;overflow:auto;}</style>");
            out.println("</head><body><h1>Initialising database pr5</h1>");
            try {
                initialise();
                out.println("<div class='ok'><strong>Success!</strong> Tables created and sample users inserted.</div>");
                out.println("<p>Default users:</p><pre>hongda / 12345\nsonghe / 23456</pre>");
            } catch (Exception ex) {
                out.println("<div class='error'><strong>Error.</strong> Could not initialise database.</div>");
                out.println("<pre>" + ex.getMessage() + "</pre>");
            }
            out.println("</body></html>");
        }
    }

    private void initialise() throws SQLException, ClassNotFoundException {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DatabaseManager.getConnection();

            dropIfExists(connection, "videos");
            dropIfExists(connection, "usuarios");

            String createUsers = "CREATE TABLE usuarios (id_usuario VARCHAR(256) PRIMARY KEY, password VARCHAR(256))";
            statement = connection.prepareStatement(createUsers);
            statement.executeUpdate();
            statement.close();

            statement = connection.prepareStatement("INSERT INTO usuarios (id_usuario, password) VALUES (?, ?)");
            statement.setString(1, "hongda");
            statement.setString(2, "12345");
            statement.executeUpdate();
            statement.setString(1, "songhe");
            statement.setString(2, "23456");
            statement.executeUpdate();
            statement.close();

            String createVideos = "CREATE TABLE videos (" +
                    "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                    "title VARCHAR(255) NOT NULL," +
                    "description VARCHAR(2000)," +
                    "uploader VARCHAR(256) NOT NULL," +
                    "upload_date DATE NOT NULL," +
                    "duration INTEGER," +
                    "original_filename VARCHAR(255)," +
                    "file_path VARCHAR(500)," +
                    "processed_path VARCHAR(500)," +
                    "status VARCHAR(20) NOT NULL," +
                    "views INTEGER DEFAULT 0," +
                    "PRIMARY KEY (id)," +
                    "FOREIGN KEY (uploader) REFERENCES usuarios(id_usuario))";
            statement = connection.prepareStatement(createVideos);
            statement.executeUpdate();
        } finally {
            DatabaseManager.closeStatement(statement);
            DatabaseManager.closeConnection(connection);
        }
    }

    private void dropIfExists(Connection connection, String table) {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("DROP TABLE " + table);
            statement.executeUpdate();
        } catch (SQLException ignore) {
            // Table might not exist.
        } finally {
            DatabaseManager.closeStatement(statement);
        }
    }
}
