package com.example.mediaforge.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.mediaforge.dto.MediaRequest;
import com.example.mediaforge.dto.MediaResponse;
import com.example.mediaforge.dto.MediaUploadResponse;
import com.example.mediaforge.service.MediaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping
    public ResponseEntity<MediaResponse> createMedia(
            @Valid @RequestBody MediaRequest request) {

        return ResponseEntity.ok(
                mediaService.saveMedia(request)
        );
    }

    @PostMapping("/upload")
    public ResponseEntity<MediaUploadResponse> uploadMedia(
            @RequestParam("file") MultipartFile file) throws IOException {

        return ResponseEntity.ok(
                mediaService.uploadMedia(file)
        );
    }

    @PostMapping("/{id}/optimize")
    public ResponseEntity<MediaResponse> optimizeMedia(
            @PathVariable Long id) throws IOException {

        return ResponseEntity.ok(
                mediaService.optimizeMedia(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<MediaResponse>> getAllMedia() {

        return ResponseEntity.ok(
                mediaService.getAllMedia()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<MediaResponse> getMediaById(
            @PathVariable Long id) {

        MediaResponse response =
                mediaService.getMediaById(id);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedia(
            @PathVariable Long id) {

        if (!mediaService.deleteMedia(id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}