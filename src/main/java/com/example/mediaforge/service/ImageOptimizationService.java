package com.example.mediaforge.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

@ Service 

    public class ImageOptimizationService {

        public long optimizeImage(Path inputPath, Path outputPath) throws IOException {

            // Make sure output directory exists
            if (outputPath.getParent() != null) {
                Files.createDirectories(outputPath.getParent());
            }

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

            StringBuilder processOutput = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                String line;

                while ((line = reader.readLine()) != null) {
                    processOutput.append(line).append(System.lineSeparator());
                }

                int exitCode = process.waitFor();

                if (exitCode != 0) {
                    throw new IOException(
                            "Image optimization failed. ImageMagick output:\n"
                            + processOutput
                    );
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Image optimization interrupted", e);
            }

            if (!Files.exists(outputPath)) {
                throw new IOException(
                        "Optimized file was not created. ImageMagick output:\n"
                        + processOutput
                );
            }

            return Files.size(outputPath);
        }
    }
    

