package model;

public class Video {
    public int id;
    public String title;
    public String description;
    public String uploader;
    public String uploadDate;
    public Integer duration;
    public String originalFilename;
    public String filePath;
    public String processedPath;
    public String status;
    public int views;

    public Video() {
    }

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

    public String getDurationFormatted() {
        if (duration == null) return "Unknown";
        int minutes = duration / 60;
        int seconds = duration % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
