package com.example.mediaforge.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.mediaforge.dto.MediaRequest;
import com.example.mediaforge.dto.MediaResponse;
import com.example.mediaforge.entity.Media;
import com.example.mediaforge.repository.MediaRepository;

@Service
public class MediaService {

    private final MediaRepository mediaRepository;

    public MediaService(MediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    public MediaResponse saveMedia(MediaRequest request) {

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

        Media savedMedia = mediaRepository.save(media);

        return toResponse(savedMedia);
    }

    public List<MediaResponse> getAllMedia() {
        return mediaRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public MediaResponse getMediaById(Long id) {
        return mediaRepository.findById(id)
                .map(this::toResponse)
                .orElse(null);
    }

    public boolean deleteMedia(Long id) {
        if (!mediaRepository.existsById(id)) {
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
                media.getCreatedAt()
        );
    }
}
