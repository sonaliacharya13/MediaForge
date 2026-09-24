package com.example.mediaforge.dto;

import java.time.LocalDateTime;

public class MediaUploadResponse {

    private final Long id;
    private final String filename;
    private final Long originalSize;
    private final String format;
    private final String status;
    private final LocalDateTime createdAt;

    public MediaUploadResponse(
            Long id,
            String filename,
            Long originalSize,
            String format,
            String status,
            LocalDateTime createdAt) {

        this.id = id;
        this.filename = filename;
        this.originalSize = originalSize;
        this.format = format;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public Long getOriginalSize() {
        return originalSize;
    }

    public String getFormat() {
        return format;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
