package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class VideoDAO {

    public static class Video {
        public final int id;
        public final String title;
        public final String description;
        public final String uploader;
        public final String uploadDate;
        public final Integer duration;
        public final String originalFilename;
        public final String filePath;
        public final String processedPath;
        public final String status;
        public final int views;

        public Video(int id, String title, String description, String uploader,
                     String uploadDate, Integer duration, String originalFilename,
                     String filePath, String processedPath, String status, int views) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.uploader = uploader;
            this.uploadDate = uploadDate;
            this.duration = duration;
            this.originalFilename = originalFilename;
            this.filePath = filePath;
            this.processedPath = processedPath;
            this.status = status;
            this.views = views;
        }
    }

    public static Video insertVideo(String title, String description, String uploader,
                                    String uploadDate, String originalFilename,
                                    Integer duration, String filePath, String status) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DatabaseManager.getConnection();
            String query = "INSERT INTO videos (title, description, uploader, upload_date, " +
                    "original_filename, duration, file_path, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, title);
            statement.setString(2, description);
            statement.setString(3, uploader);
            statement.setDate(4, Date.valueOf(uploadDate));
            statement.setString(5, originalFilename);
            if (duration != null) {
                statement.setInt(6, duration);
            } else {
                statement.setNull(6, java.sql.Types.INTEGER);
            }
            statement.setString(7, filePath);
            statement.setString(8, status);

            int inserted = statement.executeUpdate();
            if (inserted == 0) {
                return null;
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    return getVideoById(id);
                }
            }
            return null;

        } catch (Exception e) {
            System.err.println("Error inserting video: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            DatabaseManager.closeStatement(statement);
            DatabaseManager.closeConnection(connection);
        }
    }

    public static Video updateVideo(int id, String uploader, String title, String description) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DatabaseManager.getConnection();
            String sql = "UPDATE videos SET title=?, description=? WHERE id=? AND uploader=?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, title);
            statement.setString(2, description);
            statement.setInt(3, id);
            statement.setString(4, uploader);

            int updated = statement.executeUpdate();
            if (updated == 0) {
                return null;
            }
            return getVideoById(id);

        } catch (Exception e) {
            System.err.println("Error updating video: " + e.getMessage());
            return null;
        } finally {
            DatabaseManager.closeStatement(statement);
            DatabaseManager.closeConnection(connection);
        }
    }

    public static boolean updateStatus(int id, String status) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DatabaseManager.getConnection();
            String query = "UPDATE videos SET status = ? WHERE id = ?";
            statement = connection.prepareStatement(query);
            statement.setString(1, status);
            statement.setInt(2, id);
            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("Error updating video status: " + e.getMessage());
            return false;
        } finally {
            DatabaseManager.closeStatement(statement);
            DatabaseManager.closeConnection(connection);
        }
    }

    public static boolean updateProcessedPath(int id, String processedPath) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DatabaseManager.getConnection();
            String query = "UPDATE videos SET processed_path = ? WHERE id = ?";
            statement = connection.prepareStatement(query);
            statement.setString(1, processedPath);
            statement.setInt(2, id);
            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("Error updating video processed path: " + e.getMessage());
            return false;
        } finally {
            DatabaseManager.closeStatement(statement);
            DatabaseManager.closeConnection(connection);
        }
    }


    public static boolean deleteVideo(int id, String uploader) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DatabaseManager.getConnection();
            String query = "DELETE FROM videos WHERE id = ? AND uploader = ?";
            statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            statement.setString(2, uploader);
            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("Error deleting video: " + e.getMessage());
            return false;
        } finally {
            DatabaseManager.closeStatement(statement);
            DatabaseManager.closeConnection(connection);
        }
    }

    public static Video getVideoById(int id) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseManager.getConnection();
            String query = "SELECT * FROM videos WHERE id = ?";
            statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return mapRow(resultSet);
            }
            return null;

        } catch (Exception e) {
            System.err.println("Error retrieving video by id: " + e.getMessage());
            return null;
        } finally {
            DatabaseManager.closeResultSet(resultSet);
            DatabaseManager.closeStatement(statement);
            DatabaseManager.closeConnection(connection);
        }
    }

    public static List<Video> searchByTitle(String title) {
        return searchWithFilter("title", title);
    }

    public static List<Video> searchByUploader(String uploader) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<Video> videos = new ArrayList<>();

        try {
            connection = DatabaseManager.getConnection();
            String query = "SELECT * FROM videos WHERE uploader = ?";
            statement = connection.prepareStatement(query);
            statement.setString(1, uploader);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                videos.add(mapRow(resultSet));
            }
            return videos;

        } catch (Exception e) {
            System.err.println("Error searching by uploader: " + e.getMessage());
            return videos;
        } finally {
            DatabaseManager.closeResultSet(resultSet);
            DatabaseManager.closeStatement(statement);
            DatabaseManager.closeConnection(connection);
        }
    }

    public static List<Video> searchByQuery(String query) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<Video> videos = new ArrayList<>();

        try {
            connection = DatabaseManager.getConnection();
            String sql = "SELECT * FROM videos WHERE title LIKE ? OR uploader LIKE ? ORDER BY upload_date DESC";
            statement = connection.prepareStatement(sql);
            String searchPattern = "%" + query + "%";
            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                videos.add(mapRow(resultSet));
            }
            return videos;

        } catch (Exception e) {
            System.err.println("Error searching videos: " + e.getMessage());
            return videos;
        } finally {
            DatabaseManager.closeResultSet(resultSet);
            DatabaseManager.closeStatement(statement);
            DatabaseManager.closeConnection(connection);
        }
    }

    public static List<Video> searchAll() {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<Video> videos = new ArrayList<>();

        try {
            connection = DatabaseManager.getConnection();
            String query = "SELECT * FROM videos ORDER BY upload_date DESC";
            statement = connection.prepareStatement(query);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                videos.add(mapRow(resultSet));
            }
            return videos;

        } catch (Exception e) {
            System.err.println("Error retrieving all videos: " + e.getMessage());
            return videos;
        } finally {
            DatabaseManager.closeResultSet(resultSet);
            DatabaseManager.closeStatement(statement);
            DatabaseManager.closeConnection(connection);
        }
    }

    private static List<Video> searchWithFilter(String column, String value) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<Video> videos = new ArrayList<>();

        try {
            connection = DatabaseManager.getConnection();
            String query = "SELECT * FROM videos WHERE " + column + " LIKE ?";
            statement = connection.prepareStatement(query);
            statement.setString(1, "%" + value + "%");
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                videos.add(mapRow(resultSet));
            }
            return videos;

        } catch (Exception e) {
            System.err.println("Error searching videos by " + column + ": " + e.getMessage());
            return videos;
        } finally {
            DatabaseManager.closeResultSet(resultSet);
            DatabaseManager.closeStatement(statement);
            DatabaseManager.closeConnection(connection);
        }
    }

    private static Video mapRow(ResultSet rs) throws SQLException {
        Date uploadDate = rs.getDate("upload_date");
        return new Video(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("uploader"),
                uploadDate != null ? uploadDate.toString() : null,
                rs.getObject("duration") != null ? rs.getInt("duration") : null,
                rs.getString("original_filename"),
                rs.getString("file_path"),
                rs.getString("processed_path"),
                rs.getString("status"),
                rs.getInt("views")
        );
    }

    public static boolean updateDuration(int id, int duration) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DatabaseManager.getConnection();
            String query = "UPDATE videos SET duration = ? WHERE id = ?";
            statement = connection.prepareStatement(query);
            statement.setInt(1, duration);
            statement.setInt(2, id);
            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("Error updating video duration: " + e.getMessage());
            return false;
        } finally {
            DatabaseManager.closeStatement(statement);
            DatabaseManager.closeConnection(connection);
        }
    }

    public static boolean incrementViews(int id) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DatabaseManager.getConnection();
            String query = "UPDATE videos SET views = views + 1 WHERE id = ?";
            statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("Error incrementing video views: " + e.getMessage());
            return false;
        } finally {
            DatabaseManager.closeStatement(statement);
            DatabaseManager.closeConnection(connection);
        }
    }
}
