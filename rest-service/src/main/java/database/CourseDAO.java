package database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {

    public static class Course {
        public int id;
        public String title;
        public String primaryLanguage;
        public String detectedLanguages;
        public Integer videoId;
        public Timestamp createdAt;
        public String status;

        public Course(int id, String title, String primaryLanguage, String detectedLanguages,
                     Integer videoId, Timestamp createdAt, String status) {
            this.id = id;
            this.title = title;
            this.primaryLanguage = primaryLanguage;
            this.detectedLanguages = detectedLanguages;
            this.videoId = videoId;
            this.createdAt = createdAt;
            this.status = status;
        }
    }

    public static Course insertCourse(String title, String primaryLanguage,
                                     Integer videoId, String status) {
        String sql = "INSERT INTO courses (title, primary_language, video_id, status) " +
                    "VALUES (?, ?, ?, ?)";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;

        try {
            connection = DatabaseManager.getConnection();
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            statement.setString(1, title);
            statement.setString(2, primaryLanguage);
            if (videoId != null) {
                statement.setInt(3, videoId);
            } else {
                statement.setNull(3, Types.INTEGER);
            }
            statement.setString(4, status);

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating course failed, no rows affected.");
            }

            generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int courseId = generatedKeys.getInt(1);
                return getCourseById(courseId);
            } else {
                throw new SQLException("Creating course failed, no ID obtained.");
            }

        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error inserting course: " + e.getMessage());
            return null;
        } finally {
            DatabaseManager.closeResultSet(generatedKeys);
            DatabaseManager.closeStatement(statement);
            DatabaseManager.closeConnection(connection);
        }
    }

    public static Course getCourseById(int courseId) {
        String sql = "SELECT id, title, primary_language, detected_languages, video_id, " +
                    "created_at, status FROM courses WHERE id = ?";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseManager.getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, courseId);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Integer videoId = null;
                int videoIdInt = resultSet.getInt("video_id");
                if (!resultSet.wasNull()) {
                    videoId = videoIdInt;
                }
                
                return new Course(
                    resultSet.getInt("id"),
                    resultSet.getString("title"),
                    resultSet.getString("primary_language"),
                    resultSet.getString("detected_languages"),
                    videoId,
                    resultSet.getTimestamp("created_at"),
                    resultSet.getString("status")
                );
            }

            return null;

        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error getting course: " + e.getMessage());
            return null;
        } finally {
            DatabaseManager.closeResultSet(resultSet);
            DatabaseManager.closeStatement(statement);
            DatabaseManager.closeConnection(connection);
        }
    }

    public static List<Course> getAllCourses() {
        String sql = "SELECT id, title, primary_language, detected_languages, video_id, " +
                    "created_at, status FROM courses ORDER BY created_at DESC";

        List<Course> courses = new ArrayList<>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseManager.getConnection();
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Integer videoId = null;
                int videoIdInt = resultSet.getInt("video_id");
                if (!resultSet.wasNull()) {
                    videoId = videoIdInt;
                }
                
                courses.add(new Course(
                    resultSet.getInt("id"),
                    resultSet.getString("title"),
                    resultSet.getString("primary_language"),
                    resultSet.getString("detected_languages"),
                    videoId,
                    resultSet.getTimestamp("created_at"),
                    resultSet.getString("status")
                ));
            }

        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error getting courses: " + e.getMessage());
        } finally {
            DatabaseManager.closeResultSet(resultSet);
            DatabaseManager.closeStatement(statement);
            DatabaseManager.closeConnection(connection);
        }

        return courses;
    }

    public static boolean updateCourseStatus(int courseId, String status) {
        String sql = "UPDATE courses SET status = ? WHERE id = ?";

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DatabaseManager.getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, status);
            statement.setInt(2, courseId);

            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error updating course status: " + e.getMessage());
            return false;
        } finally {
            DatabaseManager.closeStatement(statement);
            DatabaseManager.closeConnection(connection);
        }
    }

    public static boolean updateDetectedLanguages(int courseId, String languages) {
        String sql = "UPDATE courses SET detected_languages = ? WHERE id = ?";

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DatabaseManager.getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, languages);
            statement.setInt(2, courseId);

            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error updating detected languages: " + e.getMessage());
            return false;
        } finally {
            DatabaseManager.closeStatement(statement);
            DatabaseManager.closeConnection(connection);
        }
    }

    public static boolean deleteCourse(int courseId) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DatabaseManager.getConnection();
            connection.setAutoCommit(false); // Start transaction

            // Delete related records first (due to foreign key constraints)
            // 1. Delete course_notes
            try {
                statement = connection.prepareStatement("DELETE FROM course_notes WHERE course_id = ?");
                statement.setInt(1, courseId);
                statement.executeUpdate();
                statement.close();
            } catch (SQLException e) {
                System.err.println("Warning: Error deleting course_notes: " + e.getMessage());
            }

            // 2. Delete transcripts
            try {
                statement = connection.prepareStatement("DELETE FROM transcripts WHERE course_id = ?");
                statement.setInt(1, courseId);
                statement.executeUpdate();
                statement.close();
            } catch (SQLException e) {
                System.err.println("Warning: Error deleting transcripts: " + e.getMessage());
            }

            // 3. Delete course_contents
            try {
                statement = connection.prepareStatement("DELETE FROM course_contents WHERE course_id = ?");
                statement.setInt(1, courseId);
                statement.executeUpdate();
                statement.close();
            } catch (SQLException e) {
                System.err.println("Warning: Error deleting course_contents: " + e.getMessage());
            }

            // 4. Finally delete the course
            statement = connection.prepareStatement("DELETE FROM courses WHERE id = ?");
            statement.setInt(1, courseId);
            int affectedRows = statement.executeUpdate();

            connection.commit(); // Commit transaction
            return affectedRows > 0;

        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error deleting course: " + e.getMessage());
            if (connection != null) {
                try {
                    connection.rollback(); // Rollback on error
                } catch (SQLException rollbackEx) {
                    System.err.println("Error rolling back transaction: " + rollbackEx.getMessage());
                }
            }
            return false;
        } finally {
            DatabaseManager.closeStatement(statement);
            if (connection != null) {
                try {
                    connection.setAutoCommit(true); // Reset auto-commit
                } catch (SQLException e) {
                    System.err.println("Error resetting auto-commit: " + e.getMessage());
                }
            }
            DatabaseManager.closeConnection(connection);
        }
    }
}
