package com.example.mediaforge.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "media")
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filename;

    private Long originalSize;

    private Long optimizedSize = 0L;

    private String format;

    private String originalPath;

    private String optimizedPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Media() {
    }

    public Media(
            String filename,
            Long originalSize,
            Long optimizedSize,
            String format,
            String originalPath,
            String optimizedPath,
            MediaStatus status,
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

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
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

    public MediaStatus getStatus() {
        return status;
    }

    public void setStatus(MediaStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}