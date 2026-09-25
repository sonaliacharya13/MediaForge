package com.example.mediaforge.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class ImageOptimizationService {

    public long optimizeImage(Path inputPath, Path outputPath) throws IOException {

        ProcessBuilder processBuilder = new ProcessBuilder(
                "magick",
                inputPath.toString(),
                "-strip",
                "-quality",
                "80",
                outputPath.toString()
        );

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        try {
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new IOException("Image optimization failed");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Image optimization interrupted", e);
        }

        if (!Files.exists(outputPath)) {
            throw new IOException("Optimized file was not created");
        }

        return Files.size(outputPath);
    }
}