package com.example.mediaforge.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "media")
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;

    private Long originalSize;

    private Long optimizedSize;

    private String format;

    private String originalPath;

    private String optimizedPath;

    private String status;

    private LocalDateTime createdAt;

    public Media() {
    }

    public Media(String filename, Long originalSize, Long optimizedSize,
            String format, String originalPath,
            String optimizedPath, String status,
            LocalDateTime createdAt) {

        this.filename = filename;
        this.originalSize = originalSize;
        this.optimizedSize = optimizedSize;
        this.format = format;
        this.originalPath = originalPath;
        this.optimizedPath = optimizedPath;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Long getOriginalSize() {
        return originalSize;
    }

    public void setOriginalSize(Long originalSize) {
        this.originalSize = originalSize;
    }

    public Long getOptimizedSize() {
        return optimizedSize;
    }

    public void setOptimizedSize(Long optimizedSize) {
        this.optimizedSize = optimizedSize;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getOriginalPath() {
        return originalPath;
    }

    public void setOriginalPath(String originalPath) {
        this.originalPath = originalPath;
    }

    public String getOptimizedPath() {
        return optimizedPath;
    }

    public void setOptimizedPath(String optimizedPath) {
        this.optimizedPath = optimizedPath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
