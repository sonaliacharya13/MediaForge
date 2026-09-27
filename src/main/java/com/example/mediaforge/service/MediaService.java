package com.example.mediaforge.service;

import com.example.mediaforge.dto.MediaRequest;
import com.example.mediaforge.dto.MediaResponse;
import com.example.mediaforge.dto.MediaUploadResponse;
import com.example.mediaforge.entity.Media;
import com.example.mediaforge.entity.MediaStatus;
import com.example.mediaforge.entity.User;
import com.example.mediaforge.repository.MediaRepository;
import com.example.mediaforge.repository.MediaSpecification;
import com.example.mediaforge.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MediaService {

    private final MediaRepository mediaRepository;
    private final FileStorageService fileStorageService;
    private final ImageOptimizationService imageOptimizationService;
    private final VideoOptimizationService videoOptimizationService;
    private final UserRepository userRepository;
    private final AsyncMediaProcessingService asyncMediaProcessingService;
    private final MediaFileValidationService mediaFileValidationService;

    public MediaService(
            MediaRepository mediaRepository,
            FileStorageService fileStorageService,
            ImageOptimizationService imageOptimizationService,
            VideoOptimizationService videoOptimizationService,
            UserRepository userRepository,
            AsyncMediaProcessingService asyncMediaProcessingService,
            MediaFileValidationService mediaFileValidationService) {

        this.mediaRepository = mediaRepository;
        this.fileStorageService = fileStorageService;
        this.imageOptimizationService = imageOptimizationService;
        this.videoOptimizationService = videoOptimizationService;
        this.userRepository = userRepository;
        this.asyncMediaProcessingService = asyncMediaProcessingService;
        this.mediaFileValidationService = mediaFileValidationService;
    }

    // =========================
    // Create Media
    // =========================

    @Transactional
    public MediaResponse saveMedia(MediaRequest request) {
        User user = getCurrentUser();

        Media media = new Media(
                request.getFilename(),
                request.getOriginalSize(),
                request.getOptimizedSize(),
                request.getFormat(),
                request.getOriginalPath(),
                request.getOptimizedPath(),
                MediaStatus.valueOf(request.getStatus().toUpperCase()),
                LocalDateTime.now()
        );

        media.setUser(user);
        Media savedMedia = mediaRepository.save(media);

        return toResponse(savedMedia);
    }

    // =========================
    // Upload Media
    // =========================

    @Transactional
    public MediaUploadResponse uploadMedia(MultipartFile file) throws IOException {
        mediaFileValidationService.validate(file);

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unnamed";
        String format = extractFormat(originalFilename);

        String originalPath = fileStorageService.storeOriginalFile(file);

        Media media = new Media();
        media.setUser(getCurrentUser());
        media.setFilename(originalFilename);
        media.setOriginalSize(file.getSize());
        media.setOptimizedSize(0L);
        media.setFormat(format);
        media.setOriginalPath(originalPath);
        media.setOptimizedPath(null);
        media.setStatus(MediaStatus.PENDING);
        media.setCreatedAt(LocalDateTime.now());

        Media savedMedia = mediaRepository.save(media);

        return new MediaUploadResponse(
                savedMedia.getId(),
                savedMedia.getFilename(),
                savedMedia.getOriginalSize(),
                savedMedia.getFormat(),
                savedMedia.getStatus().name(),
                savedMedia.getCreatedAt()
        );
    }

    // =========================
    // Start Optimization
    // =========================

    @Transactional
    public MediaResponse optimizeMedia(Long id) {
        Media media = mediaRepository
                .findByIdAndUserUsername(id, getCurrentUsername())
                .orElseThrow(() -> new IllegalArgumentException("Media not found"));

        if (MediaStatus.PROCESSING == media.getStatus()) {
            throw new IllegalArgumentException("Media is already being optimized");
        }

        if (MediaStatus.COMPLETED == media.getStatus()) {
            throw new IllegalArgumentException("Media has already been optimized");
        }

        media.setStatus(MediaStatus.PROCESSING);
        Media savedMedia = mediaRepository.save(media);

        asyncMediaProcessingService.processMedia(savedMedia);

        return toResponse(savedMedia);
    }

    // =========================
    // Get All Media
    // =========================

    @Transactional(readOnly = true)
    public List<MediaResponse> getAllMedia() {
        return mediaRepository
                .findByUserUsernameOrderByCreatedAtDesc(
                        getCurrentUsername(),
                        PageRequest.of(0, 100)
                )
                .getContent()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =========================
    // Get Media By ID
    // =========================

    @Transactional(readOnly = true)
    public MediaResponse getMediaById(Long id) {
        return mediaRepository
                .findByIdAndUserUsername(id, getCurrentUsername())
                .map(this::toResponse)
                .orElse(null);
    }

    // =========================
    // Delete Media
    // =========================

    @Transactional
    public boolean deleteMedia(Long id) {
        String username = getCurrentUsername();

        return mediaRepository.findByIdAndUserUsername(id, username)
                .map(media -> {
                    mediaRepository.delete(media);
                    return true;
                })
                .orElse(false);
    }

    // =========================
    // Media History
    // =========================

    @Transactional(readOnly = true)
    public Page<MediaResponse> getMediaHistory(Pageable pageable) {
        return mediaRepository
                .findByUserUsernameOrderByCreatedAtDesc(getCurrentUsername(), pageable)
                .map(this::toResponse);
    }

    // =========================
    // Search Media
    // =========================

    @Transactional(readOnly = true)
    public Page<MediaResponse> searchMedia(
            String filename,
            String format,
            String status,
            Pageable pageable) {

        String username = getCurrentUsername();
        Specification<Media> specification = MediaSpecification.belongsToUser(username);

        if (filename != null && !filename.isBlank()) {
            specification = specification.and(MediaSpecification.filenameContains(filename));
        }

        if (format != null && !format.isBlank()) {
            specification = specification.and(MediaSpecification.formatEquals(format));
        }

        if (status != null && !status.isBlank()) {
            specification = specification.and(MediaSpecification.statusEquals(status));
        }

        return mediaRepository.findAll(specification, pageable).map(this::toResponse);
    }

    // =========================
    // Download Optimized Media
    // =========================

    @Transactional(readOnly = true)
    public Path getOptimizedMedia(Long id) {
        Media media = mediaRepository
                .findByIdAndUserUsername(id, getCurrentUsername())
                .orElseThrow(() -> new IllegalArgumentException("Media not found"));

        if (media.getOptimizedPath() == null) {
            throw new IllegalArgumentException("Media has not been optimized yet");
        }

        Path filePath = fileStorageService.getFile(media.getOptimizedPath());

        if (filePath == null || !Files.exists(filePath)) {
            throw new IllegalArgumentException("Optimized file not found");
        }

        return filePath;
    }

    // =========================
    // Helpers
    // =========================

    private String extractFormat(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1).toLowerCase();
        }
        return "unknown";
    }

    private MediaResponse toResponse(Media media) {
    return new MediaResponse(
            media.getId(),
            media.getFilename(),
            media.getOriginalSize(),
            media.getOptimizedSize(),
            media.getFormat(),
            media.getStatus() != null ? media.getStatus().name() : null,
            media.getCreatedAt(),
            calculateCompressionPercentage(
                    media.getOriginalSize(),
                    media.getOptimizedSize()
            )
    );
}

    private double calculateCompressionPercentage(Long originalSize, Long optimizedSize) {
        if (originalSize == null || originalSize == 0 || optimizedSize == null || optimizedSize == 0) {
            return 0.0;
        }

        double percentage = ((double) (originalSize - optimizedSize) / originalSize) * 100.0;
        return Math.round(percentage * 100.0) / 100.0;
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found in security context");
        }
        return authentication.getName();
    }

    private User getCurrentUser() {
        return userRepository
                .findByUsername(getCurrentUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}