package com.example.mediaforge.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private final Path originalStoragePath;
    private final Path optimizedStoragePath;

    public FileStorageService(
            @Value("${media.storage.original}") String originalPath,
            @Value("${media.storage.optimized}") String optimizedPath) {

        this.originalStoragePath = Paths.get(originalPath)
                .toAbsolutePath()
                .normalize();

        this.optimizedStoragePath = Paths.get(optimizedPath)
                .toAbsolutePath()
                .normalize();
    }

    public String storeOriginalFile(MultipartFile file) throws IOException {

        Files.createDirectories(originalStoragePath);

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Invalid filename");
        }

        String extension = "";

        int dotIndex = originalFilename.lastIndexOf('.');

        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
        }

        String filename = UUID.randomUUID() + extension;

        Path targetPath = originalStoragePath.resolve(filename);

        Files.copy(
                file.getInputStream(),
                targetPath,
                StandardCopyOption.REPLACE_EXISTING
        );

        return targetPath.toString();
    }

    public Path getOptimizedStoragePath() throws IOException {
        Files.createDirectories(optimizedStoragePath);
        return optimizedStoragePath;
    }

    public Path getFile(String filePath) {
        return Paths.get(filePath).toAbsolutePath().normalize();
    }
}
