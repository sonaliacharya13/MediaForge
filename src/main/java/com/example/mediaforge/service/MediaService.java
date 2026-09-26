package com.example.mediaforge.service;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.mediaforge.dto.MediaRequest;
import com.example.mediaforge.dto.MediaResponse;
import com.example.mediaforge.dto.MediaUploadResponse;
import com.example.mediaforge.entity.Media;
import com.example.mediaforge.entity.User;
import com.example.mediaforge.repository.MediaRepository;
import com.example.mediaforge.repository.UserRepository;

@Service
public class MediaService {

    private final MediaRepository mediaRepository;
    private final FileStorageService fileStorageService;
    private final ImageOptimizationService imageOptimizationService;
    private final VideoOptimizationService videoOptimizationService;
    private final UserRepository userRepository;

    public MediaService(MediaRepository mediaRepository, FileStorageService fileStorageService, ImageOptimizationService imageOptimizationService, VideoOptimizationService videoOptimizationService, UserRepository userRepository) {
        this.mediaRepository = mediaRepository;
        this.fileStorageService = fileStorageService;
        this.imageOptimizationService = imageOptimizationService;
        this.videoOptimizationService = videoOptimizationService;
        this.userRepository = userRepository;
    }

    public MediaResponse saveMedia(MediaRequest request) {

        User user = getCurrentUser();
        Media media = new Media(
                request.getFilename(),
                request.getOriginalSize(),
                request.getOptimizedSize(),
                request.getFormat(),
                request.getOriginalPath(),
                request.getOptimizedPath(),
                request.getStatus(),
                LocalDateTime.now()
        );

        media.setUser(user);

        Media savedMedia = mediaRepository.save(media);

        return toResponse(savedMedia);
    }

    public MediaResponse optimizeImage(Long id) throws IOException {

        Media media = mediaRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Media not found"));

        Path inputPath = Path.of(media.getOriginalPath());

        Path optimizedDirectory = fileStorageService.getOptimizedStoragePath();

        String optimizedFilename = "optimized_" + media.getFilename();

        Path outputPath = optimizedDirectory.resolve(optimizedFilename);

        media.setStatus("PROCESSING");
        mediaRepository.save(media);

        long optimizedSize = imageOptimizationService.optimizeImage(inputPath, outputPath);

        media.setOptimizedSize(optimizedSize);
        media.setOptimizedPath(outputPath.toString());
        media.setStatus("COMPLETED");

        Media savedMedia = mediaRepository.save(media);

        return new MediaResponse(savedMedia.getId(), savedMedia.getFilename(), savedMedia.getOriginalSize(), savedMedia.getOptimizedSize(), savedMedia.getFormat(), savedMedia.getStatus(), savedMedia.getCreatedAt(), calculateCompressionPercentage(savedMedia.getOriginalSize(), savedMedia.getOptimizedSize()));
    }

    private double calculateCompressionPercentage(Long originalSize, Long optimizedSize) {

        if (originalSize == null || originalSize == 0 || optimizedSize == null) {
            return 0.0;
        }

        double percentage = ((double) (originalSize - optimizedSize) / originalSize) * 100;
        return Math.round(percentage * 100.0) / 100.0;
    }

    public MediaUploadResponse uploadMedia(MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String originalFilename = file.getOriginalFilename();

        String format = "unknown";

        if (originalFilename != null && originalFilename.contains(".")) {
            format = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        }

        String originalPath = fileStorageService.storeOriginalFile(file);

        Media media = new Media();

        media.setUser(getCurrentUser());
        media.setFilename(originalFilename);
        media.setOriginalSize(file.getSize());
        media.setOptimizedSize(0L);
        media.setFormat(format);
        media.setOriginalPath(originalPath);
        media.setOptimizedPath(null);
        media.setStatus("PENDING");
        media.setCreatedAt(java.time.LocalDateTime.now());

        Media savedMedia = mediaRepository.save(media);

        return new MediaUploadResponse(savedMedia.getId(), savedMedia.getFilename(), savedMedia.getOriginalSize(), savedMedia.getFormat(), savedMedia.getStatus(), savedMedia.getCreatedAt());
    }

    public List<MediaResponse> getAllMedia() {

        String username = getCurrentUsername();

        return mediaRepository
                .findByUserUsernameOrderByCreatedAtDesc(
                        username,
                        org.springframework.data.domain.PageRequest.of(0, 100)
                )
                .getContent()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public MediaResponse getMediaById(Long id) {

        return mediaRepository
                .findByIdAndUserUsername(id, getCurrentUsername())
                .map(this::toResponse)
                .orElse(null);
    }

    public boolean deleteMedia(Long id) {

        String username = getCurrentUsername();

        if (!mediaRepository.findByIdAndUserUsername(id, username).isPresent()) {
            return false;
        }

        mediaRepository.deleteById(id);
        return true;
    }

    private MediaResponse toResponse(Media media) {
        return new MediaResponse(
                media.getId(),
                media.getFilename(),
                media.getOriginalSize(),
                media.getOptimizedSize(),
                media.getFormat(),
                media.getStatus(),
                media.getCreatedAt(),
                calculateCompressionPercentage(
                        media.getOriginalSize(),
                        media.getOptimizedSize()
                )
        );
    }

    public MediaResponse optimizeMedia(Long id) throws IOException {

        Media media = mediaRepository
                .findByIdAndUserUsername(id, getCurrentUsername())
                .orElseThrow(()
                        -> new IllegalArgumentException("Media not found"));

        try {
            Path inputPath = Path.of(media.getOriginalPath());

            Path optimizedDirectory
                    = fileStorageService.getOptimizedStoragePath();

            String optimizedFilename
                    = "optimized_" + media.getFilename();

            Path outputPath
                    = optimizedDirectory.resolve(optimizedFilename);

            media.setStatus("PROCESSING");
            mediaRepository.save(media);

            long optimizedSize;

            String format = media.getFormat().toLowerCase();

            if (format.equals("jpg")
                    || format.equals("jpeg")
                    || format.equals("png")
                    || format.equals("webp")) {

                optimizedSize = imageOptimizationService.optimizeImage(
                        inputPath,
                        outputPath
                );

            } else if (format.equals("mp4")
                    || format.equals("mov")
                    || format.equals("avi")
                    || format.equals("mkv")
                    || format.equals("webm")) {

                optimizedSize = videoOptimizationService.optimizeVideo(
                        inputPath,
                        outputPath
                );

            } else {
                throw new IllegalArgumentException(
                        "Unsupported media format: " + format
                );
            }

            media.setOptimizedSize(optimizedSize);
            media.setOptimizedPath(outputPath.toString());
            media.setStatus("COMPLETED");

            Media savedMedia = mediaRepository.save(media);

            return toResponse(savedMedia);

        } catch (Exception e) {

            media.setStatus("FAILED");
            mediaRepository.save(media);

            throw e;
        }
    }

    public Path getOptimizedMedia(Long id) {

        Media media = mediaRepository
                .findByIdAndUserUsername(id, getCurrentUsername())
                .orElseThrow(()
                        -> new IllegalArgumentException("Media not found"));

        if (media.getOptimizedPath() == null) {
            throw new IllegalArgumentException("Media has not been optimized yet");
        }

        Path filePath = fileStorageService.getFile(media.getOptimizedPath());

        if (!java.nio.file.Files.exists(filePath)) {
            throw new IllegalArgumentException("Optimized file not found");
        }

        return filePath;
    }

    public Page<MediaResponse> getMediaHistory(Pageable pageable) {

        return mediaRepository
                .findByUserUsernameOrderByCreatedAtDesc(
                        getCurrentUsername(),
                        pageable
                )
                .map(this::toResponse);
    }

    public Page<MediaResponse> searchMedia(
            String filename,
            String format,
            String status,
            Pageable pageable) {

        String username = getCurrentUsername();

        if (filename != null && !filename.isBlank()) {
            return mediaRepository
                    .findByUserUsernameAndFilenameContainingIgnoreCase(
                            username, filename, pageable)
                    .map(this::toResponse);
        }

        if (format != null && !format.isBlank()) {
            return mediaRepository
                    .findByUserUsernameAndFormatIgnoreCase(
                            username, format, pageable)
                    .map(this::toResponse);
        }

        if (status != null && !status.isBlank()) {
            return mediaRepository
                    .findByUserUsernameAndStatusIgnoreCase(
                            username, status, pageable)
                    .map(this::toResponse);
        }

        return mediaRepository
                .findByUserUsernameOrderByCreatedAtDesc(
                        username, pageable)
                .map(this::toResponse);
    }

    private String getCurrentUsername() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return authentication.getName();
    }

    private User getCurrentUser() {

        return userRepository.findByUsername(getCurrentUsername()).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

}
