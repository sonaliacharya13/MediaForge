package com.example.mediaforge.dto;

public class MediaRequest {

    private String filename;
    private Long originalSize;
    private Long optimizedSize;
    private String format;
    private String originalPath;
    private String optimizedPath;
    private String status;

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
}
