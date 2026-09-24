package com.example.mediaforge.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mediaforge.entity.Media;
import com.example.mediaforge.service.MediaService;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping
    public ResponseEntity<Media> createMedia(@RequestBody Media media) {
        Media savedMedia = mediaService.saveMedia(media);
        return ResponseEntity.ok(savedMedia);
    }

    @GetMapping
    public ResponseEntity<List<Media>> getAllMedia() {
        return ResponseEntity.ok(mediaService.getAllMedia());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Media> getMediaById(@PathVariable Long id) {
        return mediaService.getMediaById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedia(@PathVariable Long id) {
        if (mediaService.getMediaById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        mediaService.deleteMedia(id);
        return ResponseEntity.noContent().build();
    }
}
