package com.example.mediaforge.service;

import com.example.mediaforge.entity.Media;
import com.example.mediaforge.repository.MediaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MediaService {

    private final MediaRepository mediaRepository;

    public MediaService(MediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    public Media saveMedia(Media media) {
        return mediaRepository.save(media);
    }

    public List<Media> getAllMedia() {
        return mediaRepository.findAll();
    }

    public Optional<Media> getMediaById(Long id) {
        return mediaRepository.findById(id);
    }

    public void deleteMedia(Long id) {
        mediaRepository.deleteById(id);
    }
}
