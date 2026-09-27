package com.example.mediaforge.service;

import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MediaFileValidationService {

    private static final Set<String> ALLOWED_FORMATS = Set.of(
            "jpg", "jpeg", "png", "webp",
            "mp4", "mov", "avi", "mkv", "webm"
    );

    public void validate(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String filename = file.getOriginalFilename();

        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException("Invalid file format");
        }

        String extension = filename
                .substring(filename.lastIndexOf('.') + 1)
                .toLowerCase();

        if (!ALLOWED_FORMATS.contains(extension)) {
            throw new IllegalArgumentException(
                    "Unsupported media format: " + extension
            );
        }
    }
}
