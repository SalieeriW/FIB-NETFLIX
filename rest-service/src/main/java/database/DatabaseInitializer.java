package database;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Automatic database initialization on application startup.
 * Only initializes if tables do not exist.
 */
@WebListener
public class DatabaseInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("=== DatabaseInitializer: Checking database schema ===");

        try {
            if (!tablesExist()) {
                System.out.println("Tables do not exist. Initializing database...");
                initializeDatabase();
                System.out.println("Database initialized successfully with default users.");
            } else {
                System.out.println("Tables already exist. Skipping initialization.");
            }
        } catch (Exception e) {
            System.err.println("ERROR: Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Cleanup if needed
    }

    /**
     * Check if the required tables exist in the database
     */
    private boolean tablesExist() throws SQLException, ClassNotFoundException {
        Connection connection = null;
        ResultSet rs = null;
        try {
            connection = DatabaseManager.getConnection();
            DatabaseMetaData metaData = connection.getMetaData();

            // Check if 'usuarios' table exists
            rs = metaData.getTables(null, null, "USUARIOS", null);
            if (!rs.next()) {
                return false;
            }
            rs.close();

            // Check if 'videos' table exists
            rs = metaData.getTables(null, null, "VIDEOS", null);
            if (!rs.next()) {
                return false;
            }
            rs.close();

            // Check if 'courses' table exists
            rs = metaData.getTables(null, null, "COURSES", null);
            if (!rs.next()) {
                return false;
            }

            return true;
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException ignored) {}
            }
            DatabaseManager.closeConnection(connection);
        }
    }

    /**
     * Initialize database schema and insert default users
     */
    private void initializeDatabase() throws SQLException, ClassNotFoundException {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DatabaseManager.getConnection();

            // Create usuarios table
            String createUsers = "CREATE TABLE usuarios (" +
                    "id_usuario VARCHAR(256) PRIMARY KEY, " +
                    "password VARCHAR(256))";
            statement = connection.prepareStatement(createUsers);
            statement.executeUpdate();
            statement.close();
            System.out.println("  ✓ Table 'usuarios' created");

            // Insert default users
            statement = connection.prepareStatement("INSERT INTO usuarios (id_usuario, password) VALUES (?, ?)");
            statement.setString(1, "hongda");
            statement.setString(2, "12345");
            statement.executeUpdate();
            System.out.println("  ✓ User 'hongda' created");

            statement.setString(1, "songhe");
            statement.setString(2, "23456");
            statement.executeUpdate();
            System.out.println("  ✓ User 'songhe' created");
            statement.close();

            // Create videos table
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
            System.out.println("  ✓ Table 'videos' created");
            statement.close();

            // Create courses table
            String createCourses = "CREATE TABLE courses (" +
                    "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                    "title VARCHAR(255) NOT NULL," +
                    "primary_language VARCHAR(10)," +
                    "detected_languages VARCHAR(50)," +
                    "video_id INTEGER," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "status VARCHAR(50)," +
                    "PRIMARY KEY (id)," +
                    "FOREIGN KEY (video_id) REFERENCES videos(id))";
            statement = connection.prepareStatement(createCourses);
            statement.executeUpdate();
            System.out.println("  ✓ Table 'courses' created");
            statement.close();

            // Create transcripts table
            String createTranscripts = "CREATE TABLE transcripts (" +
                    "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                    "course_id INTEGER," +
                    "full_text CLOB," +
                    "segments CLOB," +
                    "primary_language VARCHAR(10)," +
                    "language_distribution CLOB," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "PRIMARY KEY (id)," +
                    "FOREIGN KEY (course_id) REFERENCES courses(id))";
            statement = connection.prepareStatement(createTranscripts);
            statement.executeUpdate();
            System.out.println("  ✓ Table 'transcripts' created");
            statement.close();

            // Create course_contents table
            String createContents = "CREATE TABLE course_contents (" +
                    "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                    "course_id INTEGER," +
                    "content_type VARCHAR(50)," +
                    "filename VARCHAR(255)," +
                    "extracted_text CLOB," +
                    "detected_language VARCHAR(10)," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "PRIMARY KEY (id)," +
                    "FOREIGN KEY (course_id) REFERENCES courses(id))";
            statement = connection.prepareStatement(createContents);
            statement.executeUpdate();
            System.out.println("  ✓ Table 'course_contents' created");
            statement.close();

            // Create course_notes table
            String createNotes = "CREATE TABLE course_notes (" +
                    "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                    "course_id INTEGER," +
                    "notes_en CLOB," +
                    "notes_es CLOB," +
                    "notes_ca CLOB," +
                    "generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "PRIMARY KEY (id)," +
                    "FOREIGN KEY (course_id) REFERENCES courses(id))";
            statement = connection.prepareStatement(createNotes);
            statement.executeUpdate();
            System.out.println("  ✓ Table 'course_notes' created");
            statement.close();

            // Create chat_history table
            String createChat = "CREATE TABLE chat_history (" +
                    "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                    "course_id INTEGER," +
                    "user_id VARCHAR(50)," +
                    "question CLOB," +
                    "question_lang VARCHAR(10)," +
                    "answer CLOB," +
                    "answer_lang VARCHAR(10)," +
                    "sources CLOB," +
                    "retrieved_langs VARCHAR(50)," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "PRIMARY KEY (id)," +
                    "FOREIGN KEY (course_id) REFERENCES courses(id))";
            statement = connection.prepareStatement(createChat);
            statement.executeUpdate();
            System.out.println("  ✓ Table 'chat_history' created");

        } finally {
            DatabaseManager.closeStatement(statement);
            DatabaseManager.closeConnection(connection);
        }
    }
}
