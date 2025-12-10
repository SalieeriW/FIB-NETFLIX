package database;

import java.sql.*;

public class TranscriptDAO {

    public static class Transcript {
        public int id;
        public int courseId;
        public String fullText;
        public String segments;
        public String primaryLanguage;
        public String languageDistribution;
        public Timestamp createdAt;

        public Transcript(int id, int courseId, String fullText, String segments,
                         String primaryLanguage, String languageDistribution, Timestamp createdAt) {
            this.id = id;
            this.courseId = courseId;
            this.fullText = fullText;
            this.segments = segments;
            this.primaryLanguage = primaryLanguage;
            this.languageDistribution = languageDistribution;
            this.createdAt = createdAt;
        }
    }

    public static Transcript insertTranscript(int courseId, String fullText, String segments,
                                             String primaryLanguage, String languageDistribution) {
        String sql = "INSERT INTO transcripts (course_id, full_text, segments, primary_language, language_distribution) " +
                    "VALUES (?, ?, ?, ?, ?)";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;

        try {
            connection = DatabaseManager.getConnection();
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            statement.setInt(1, courseId);
            statement.setString(2, fullText);
            statement.setString(3, segments);
            statement.setString(4, primaryLanguage);
            statement.setString(5, languageDistribution);

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating transcript failed, no rows affected.");
            }

            generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int transcriptId = generatedKeys.getInt(1);
                return getTranscriptById(transcriptId);
            } else {
                throw new SQLException("Creating transcript failed, no ID obtained.");
            }

        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error inserting transcript: " + e.getMessage());
            return null;
        } finally {
            DatabaseManager.closeResultSet(generatedKeys);
            DatabaseManager.closeStatement(statement);
            DatabaseManager.closeConnection(connection);
        }
    }

    public static Transcript getTranscriptById(int transcriptId) {
        String sql = "SELECT id, course_id, full_text, segments, primary_language, " +
                    "language_distribution, created_at FROM transcripts WHERE id = ?";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseManager.getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, transcriptId);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return new Transcript(
                    resultSet.getInt("id"),
                    resultSet.getInt("course_id"),
                    resultSet.getString("full_text"),
                    resultSet.getString("segments"),
                    resultSet.getString("primary_language"),
                    resultSet.getString("language_distribution"),
                    resultSet.getTimestamp("created_at")
                );
            }

            return null;

        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error getting transcript: " + e.getMessage());
            return null;
        } finally {
            DatabaseManager.closeResultSet(resultSet);
            DatabaseManager.closeStatement(statement);
            DatabaseManager.closeConnection(connection);
        }
    }

    public static Transcript getTranscriptByCourseId(int courseId) {
        String sql = "SELECT id, course_id, full_text, segments, primary_language, " +
                    "language_distribution, created_at FROM transcripts WHERE course_id = ?";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseManager.getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, courseId);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return new Transcript(
                    resultSet.getInt("id"),
                    resultSet.getInt("course_id"),
                    resultSet.getString("full_text"),
                    resultSet.getString("segments"),
                    resultSet.getString("primary_language"),
                    resultSet.getString("language_distribution"),
                    resultSet.getTimestamp("created_at")
                );
            }

            return null;

        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error getting transcript by course: " + e.getMessage());
            return null;
        } finally {
            DatabaseManager.closeResultSet(resultSet);
            DatabaseManager.closeStatement(statement);
            DatabaseManager.closeConnection(connection);
        }
    }
}
