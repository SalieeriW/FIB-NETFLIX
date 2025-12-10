package service;

import database.VideoDAO;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TranscodingService {

    private static final ExecutorService executor = Executors.newFixedThreadPool(2);
    private static final String FFMPEG_PATH = "ffmpeg";

    public static void transcodeVideoAsync(int videoId, String inputPath, String outputDir) {
        executor.submit(() -> {
            try {
                System.out.println("Starting transcoding for video ID: " + videoId);
                VideoDAO.updateStatus(videoId, "PROCESSING");

                File inputFile = new File(inputPath);
                if (!inputFile.exists()) {
                    System.err.println("Input file not found: " + inputPath);
                    VideoDAO.updateStatus(videoId, "ERROR");
                    return;
                }

                File outputDirectory = new File(outputDir);
                if (!outputDirectory.exists()) {
                    outputDirectory.mkdirs();
                }

                String baseFilename = inputFile.getName().replaceFirst("[.][^.]+$", "");
                String dashOutputPath = outputDir + File.separator + "manifest.mpd";

                // Extract real duration from video
                Integer realDuration = extractDuration(inputPath);
                if (realDuration != null) {
                    VideoDAO.updateDuration(videoId, realDuration);
                    System.out.println("Video duration detected: " + realDuration + " seconds");
                }

                // Generate thumbnail
                generateThumbnail(inputPath, outputDir, videoId);

                // Progressive transcoding: 360p first for quick playback
                boolean q360 = transcode360p(inputPath, outputDir, baseFilename);

                if (q360) {
                    // Generate initial manifest with 360p only
                    boolean dash360 = generatePartialDashManifest(outputDir, baseFilename, dashOutputPath, "360p");
                    if (dash360) {
                        VideoDAO.updateProcessedPath(videoId, "/tmp/vidstream/videos/processed/" + videoId);
                        VideoDAO.updateStatus(videoId, "PARTIAL_READY");
                        System.out.println("360p transcoding completed for video ID: " + videoId + " - Video is playable");
                    }

                    // Continue with 720p
                    boolean q720 = transcode720p(inputPath, outputDir, baseFilename);
                    if (q720) {
                        generatePartialDashManifest(outputDir, baseFilename, dashOutputPath, "360p,720p");
                        System.out.println("720p transcoding completed for video ID: " + videoId);
                    }

                    // Finally 1080p
                    boolean q1080 = transcode1080p(inputPath, outputDir, baseFilename);
                    if (q1080) {
                        // Generate final manifest with all qualities
                        boolean dashSuccess = generateDashManifest(outputDir, baseFilename, dashOutputPath);
                        if (dashSuccess) {
                            VideoDAO.updateStatus(videoId, "READY");
                            System.out.println("Full transcoding completed for video ID: " + videoId);
                        }
                    } else {
                        // 1080p failed but 360p+720p available
                        VideoDAO.updateStatus(videoId, "READY");
                        System.out.println("Transcoding completed (360p+720p) for video ID: " + videoId);
                    }
                } else {
                    VideoDAO.updateStatus(videoId, "ERROR");
                    System.err.println("360p transcoding failed for video ID: " + videoId);
                }

            } catch (Exception e) {
                System.err.println("Error during transcoding: " + e.getMessage());
                e.printStackTrace();
                VideoDAO.updateStatus(videoId, "ERROR");
            }
        });
    }

    private static boolean generatePartialDashManifest(String outputDir, String baseFilename,
                                                       String dashOutputPath, String qualities) {
        // Generate DASH manifest with only available qualities
        // qualities format: "360p" or "360p,720p" or "360p,720p,1080p"
        String[] qualityList = qualities.split(",");

        try {
            java.util.List<String> inputs = new java.util.ArrayList<>();
            for (String q : qualityList) {
                inputs.add(outputDir + File.separator + baseFilename + "_" + q + ".mp4");
            }

            // Check if first video has audio
            boolean hasAudio = hasAudioStream(inputs.get(0));

            // Build FFmpeg command for partial manifest
            java.util.List<String> command = new java.util.ArrayList<>();
            command.add(FFMPEG_PATH);

            for (String input : inputs) {
                command.add("-i");
                command.add(input);
            }

            // Map video streams
            for (int i = 0; i < qualityList.length; i++) {
                command.add("-map");
                command.add(i + ":v");
            }

            // Map audio streams only if they exist
            if (hasAudio) {
                for (int i = 0; i < qualityList.length; i++) {
                    command.add("-map");
                    command.add(i + ":a?");
                }
            }

            command.add("-c");
            command.add("copy");
            command.add("-f");
            command.add("dash");
            command.add("-seg_duration");
            command.add("4");
            command.add("-use_template");
            command.add("1");
            command.add("-use_timeline");
            command.add("1");
            
            // Adaptation sets
            if (hasAudio) {
                StringBuilder adaptationSets = new StringBuilder("id=0,streams=");
                for (int i = 0; i < qualityList.length; i++) {
                    if (i > 0) adaptationSets.append(",");
                    adaptationSets.append(i);
                }
                adaptationSets.append(" id=1,streams=");
                for (int i = 0; i < qualityList.length; i++) {
                    if (i > 0) adaptationSets.append(",");
                    adaptationSets.append(qualityList.length + i);
                }
                command.add("-adaptation_sets");
                command.add(adaptationSets.toString());
            } else {
                StringBuilder adaptationSets = new StringBuilder("id=0,streams=");
                for (int i = 0; i < qualityList.length; i++) {
                    if (i > 0) adaptationSets.append(",");
                    adaptationSets.append(i);
                }
                command.add("-adaptation_sets");
                command.add(adaptationSets.toString());
            }
            
            command.add(dashOutputPath);
            command.add("-y");

            return executeFFmpegCommand(command.toArray(new String[0]));
        } catch (Exception e) {
            System.err.println("Error generating partial DASH manifest: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static boolean transcode360p(String inputPath, String outputDir, String baseFilename) {
        String outputPath = outputDir + File.separator + baseFilename + "_360p.mp4";
        String[] command = {
            FFMPEG_PATH, "-i", inputPath,
            "-vf", "scale=640:360",
            "-c:v", "libx264",
            "-b:v", "800k",
            "-preset", "fast",
            "-c:a", "aac",
            "-b:a", "96k",
            outputPath,
            "-y"
        };
        return executeFFmpegCommand(command);
    }

    private static boolean transcode720p(String inputPath, String outputDir, String baseFilename) {
        String outputPath = outputDir + File.separator + baseFilename + "_720p.mp4";
        String[] command = {
            FFMPEG_PATH, "-i", inputPath,
            "-vf", "scale=1280:720",
            "-c:v", "libx264",
            "-b:v", "2500k",
            "-preset", "fast",
            "-c:a", "aac",
            "-b:a", "128k",
            outputPath,
            "-y"
        };
        return executeFFmpegCommand(command);
    }

    private static boolean transcode1080p(String inputPath, String outputDir, String baseFilename) {
        String outputPath = outputDir + File.separator + baseFilename + "_1080p.mp4";
        String[] command = {
            FFMPEG_PATH, "-i", inputPath,
            "-vf", "scale=1920:1080",
            "-c:v", "libx264",
            "-b:v", "5000k",
            "-preset", "fast",
            "-c:a", "aac",
            "-b:a", "192k",
            outputPath,
            "-y"
        };
        return executeFFmpegCommand(command);
    }

    private static boolean generateDashManifest(String outputDir, String baseFilename, String manifestPath) {
        String input360 = outputDir + File.separator + baseFilename + "_360p.mp4";
        String input720 = outputDir + File.separator + baseFilename + "_720p.mp4";
        String input1080 = outputDir + File.separator + baseFilename + "_1080p.mp4";

        // Check if videos have audio streams
        boolean hasAudio = hasAudioStream(input360);

        java.util.List<String> command = new java.util.ArrayList<>();
        command.add(FFMPEG_PATH);
        command.add("-i");
        command.add(input360);
        command.add("-i");
        command.add(input720);
        command.add("-i");
        command.add(input1080);
        
        // Map video streams
        command.add("-map");
        command.add("0:v");
        command.add("-map");
        command.add("1:v");
        command.add("-map");
        command.add("2:v");
        
        // Map audio streams only if they exist
        if (hasAudio) {
            command.add("-map");
            command.add("0:a?");
            command.add("-map");
            command.add("1:a?");
            command.add("-map");
            command.add("2:a?");
        }
        
        command.add("-c");
        command.add("copy");
        command.add("-f");
        command.add("dash");
        command.add("-seg_duration");
        command.add("4");
        command.add("-use_template");
        command.add("1");
        command.add("-use_timeline");
        command.add("1");
        
        // Adaptation sets: video only, or video + audio if available
        if (hasAudio) {
            command.add("-adaptation_sets");
            command.add("id=0,streams=0,1,2 id=1,streams=3,4,5");
        } else {
            command.add("-adaptation_sets");
            command.add("id=0,streams=0,1,2");
        }
        
        command.add(manifestPath);
        command.add("-y");

        return executeFFmpegCommand(command.toArray(new String[0]));
    }
    
    private static boolean hasAudioStream(String videoPath) {
        try {
            String[] command = {
                "ffprobe", "-v", "error", "-select_streams", "a:0",
                "-show_entries", "stream=codec_name", "-of", "default=noprint_wrappers=1:nokey=1",
                videoPath
            };
            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            process.waitFor();
            return line != null && !line.trim().isEmpty();
        } catch (Exception e) {
            // If we can't check, assume no audio to be safe
            return false;
        }
    }

    private static boolean executeFFmpegCommand(String[] command) {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[FFmpeg] " + line);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("FFmpeg command completed successfully");
                return true;
            } else {
                System.err.println("FFmpeg command failed with exit code: " + exitCode);
                return false;
            }

        } catch (Exception e) {
            System.err.println("Error executing FFmpeg: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static Integer extractDuration(String inputPath) {
        String[] command = {
            "ffprobe",
            "-v", "error",
            "-show_entries", "format=duration",
            "-of", "default=noprint_wrappers=1:nokey=1",
            inputPath
        };

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String durationStr = reader.readLine();

            int exitCode = process.waitFor();
            if (exitCode == 0 && durationStr != null && !durationStr.isEmpty()) {
                double durationSeconds = Double.parseDouble(durationStr);
                return (int) Math.round(durationSeconds);
            } else {
                System.err.println("Failed to extract duration from video");
                return null;
            }

        } catch (Exception e) {
            System.err.println("Error extracting duration: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generate a thumbnail from the video at the 5 second mark
     */
    public static boolean generateThumbnail(String inputPath, String outputDir, int videoId) {
        try {
            File outputDirectory = new File(outputDir);
            if (!outputDirectory.exists()) {
                outputDirectory.mkdirs();
            }

            String thumbnailPath = outputDir + File.separator + "thumbnail.jpg";
            
            // FFmpeg command to extract thumbnail at 5 seconds (or 10% of video if shorter)
            String[] command = {
                FFMPEG_PATH,
                "-i", inputPath,
                "-ss", "00:00:05",  // Seek to 5 seconds
                "-vframes", "1",    // Extract 1 frame
                "-vf", "scale=640:-1", // Scale to 640px width, maintain aspect ratio
                "-q:v", "2",        // High quality
                "-y",               // Overwrite output file
                thumbnailPath
            };

            System.out.println("Generating thumbnail for video ID: " + videoId);
            boolean success = executeFFmpegCommand(command);
            
            if (success) {
                File thumbnailFile = new File(thumbnailPath);
                if (thumbnailFile.exists()) {
                    System.out.println("Thumbnail generated successfully: " + thumbnailPath);
                    return true;
                }
            }
            
            // Fallback: try at 1 second if 5 seconds fails
            String[] fallbackCommand = {
                FFMPEG_PATH,
                "-i", inputPath,
                "-ss", "00:00:01",
                "-vframes", "1",
                "-vf", "scale=640:-1",
                "-q:v", "2",
                "-y",
                thumbnailPath
            };
            
            return executeFFmpegCommand(fallbackCommand);
            
        } catch (Exception e) {
            System.err.println("Error generating thumbnail: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static void shutdown() {
        executor.shutdown();
    }
}
