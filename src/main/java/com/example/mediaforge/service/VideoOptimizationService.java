package com.example.mediaforge.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class VideoOptimizationService {

    public long optimizeVideo(Path inputPath, Path outputPath) throws IOException {

        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-i",
                inputPath.toString(),
                "-c:v",
                "libx264",
                "-crf",
                "28",
                "-preset",
                "medium",
                "-c:a",
                "aac",
                "-b:a",
                "128k",
                outputPath.toString()
        );

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        try {
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new IOException("Video optimization failed");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Video optimization interrupted", e);
        }

        if (!Files.exists(outputPath)) {
            throw new IOException("Optimized video was not created");
        }

        return Files.size(outputPath);
    }
}