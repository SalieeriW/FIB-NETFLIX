package database;

import java.sql.*;

public class CourseNotesDAO {

    public static class CourseNotes {
        public int id;
        public int courseId;
        public String notesEn;
        public String notesEs;
        public String notesCa;
        public Timestamp generatedAt;

        public CourseNotes(int id, int courseId, String notesEn, String notesEs,
                          String notesCa, Timestamp generatedAt) {
            this.id = id;
            this.courseId = courseId;
            this.notesEn = notesEn;
            this.notesEs = notesEs;
            this.notesCa = notesCa;
            this.generatedAt = generatedAt;
        }
    }

    public static CourseNotes insertNotes(int courseId, String notesEn,
                                         String notesEs, String notesCa) {
        String sql = "INSERT INTO course_notes (course_id, notes_en, notes_es, notes_ca) " +
                    "VALUES (?, ?, ?, ?)";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;

        try {
            connection = DatabaseManager.getConnection();
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            statement.setInt(1, courseId);
            statement.setString(2, notesEn);
            statement.setString(3, notesEs);
            statement.setString(4, notesCa);

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating notes failed, no rows affected.");
            }

            generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int notesId = generatedKeys.getInt(1);
                return getNotesById(notesId);
            } else {
                throw new SQLException("Creating notes failed, no ID obtained.");
            }

        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error inserting notes: " + e.getMessage());
            return null;
        } finally {
            DatabaseManager.closeResultSet(generatedKeys);
            DatabaseManager.closeStatement(statement);
            DatabaseManager.closeConnection(connection);
        }
    }

    public static CourseNotes getNotesById(int notesId) {
        String sql = "SELECT id, course_id, notes_en, notes_es, notes_ca, generated_at " +
                    "FROM course_notes WHERE id = ?";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseManager.getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, notesId);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return new CourseNotes(
                    resultSet.getInt("id"),
                    resultSet.getInt("course_id"),
                    resultSet.getString("notes_en"),
                    resultSet.getString("notes_es"),
                    resultSet.getString("notes_ca"),
                    resultSet.getTimestamp("generated_at")
                );
            }

            return null;

        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error getting notes: " + e.getMessage());
            return null;
        } finally {
            DatabaseManager.closeResultSet(resultSet);
            DatabaseManager.closeStatement(statement);
            DatabaseManager.closeConnection(connection);
        }
    }

    public static CourseNotes getNotesByCourseId(int courseId) {
        String sql = "SELECT id, course_id, notes_en, notes_es, notes_ca, generated_at " +
                    "FROM course_notes WHERE course_id = ?";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseManager.getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, courseId);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return new CourseNotes(
                    resultSet.getInt("id"),
                    resultSet.getInt("course_id"),
                    resultSet.getString("notes_en"),
                    resultSet.getString("notes_es"),
                    resultSet.getString("notes_ca"),
                    resultSet.getTimestamp("generated_at")
                );
            }

            return null;

        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error getting notes by course: " + e.getMessage());
            return null;
        } finally {
            DatabaseManager.closeResultSet(resultSet);
            DatabaseManager.closeStatement(statement);
            DatabaseManager.closeConnection(connection);
        }
    }

    public static boolean updateNotes(int courseId, String language, String notes) {
        String column = switch(language) {
            case "en" -> "notes_en";
            case "es" -> "notes_es";
            case "ca" -> "notes_ca";
            default -> "notes_en";
        };

        String sql = "UPDATE course_notes SET " + column + " = ? WHERE course_id = ?";

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DatabaseManager.getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, notes);
            statement.setInt(2, courseId);

            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error updating notes: " + e.getMessage());
            return false;
        } finally {
            DatabaseManager.closeStatement(statement);
            DatabaseManager.closeConnection(connection);
        }
    }
}
