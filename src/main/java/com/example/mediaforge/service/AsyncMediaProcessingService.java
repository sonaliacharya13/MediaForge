package com.example.mediaforge.service;

import java.nio.file.Path;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.mediaforge.entity.Media;
import com.example.mediaforge.repository.MediaRepository;

@Service
public class AsyncMediaProcessingService {

    private final MediaRepository mediaRepository;
    private final FileStorageService fileStorageService;
    private final ImageOptimizationService imageOptimizationService;
    private final VideoOptimizationService videoOptimizationService;

    public AsyncMediaProcessingService(
            MediaRepository mediaRepository,
            FileStorageService fileStorageService,
            ImageOptimizationService imageOptimizationService,
            VideoOptimizationService videoOptimizationService) {

        this.mediaRepository = mediaRepository;
        this.fileStorageService = fileStorageService;
        this.imageOptimizationService = imageOptimizationService;
        this.videoOptimizationService = videoOptimizationService;
    }

    @Async
    public void processMedia(Media media) {

        try {
            media.setStatus("PROCESSING");
            mediaRepository.save(media);

            Path inputPath = Path.of(media.getOriginalPath());

            Path optimizedDirectory
                    = fileStorageService.getOptimizedStoragePath();

            Path outputPath = optimizedDirectory.resolve(
                    "optimized_" + media.getFilename()
            );

            String format = media.getFormat().toLowerCase();

            long optimizedSize;

            if (format.equals("jpg")
                    || format.equals("jpeg")
                    || format.equals("png")
                    || format.equals("webp")) {

                optimizedSize
                        = imageOptimizationService.optimizeImage(
                                inputPath,
                                outputPath
                        );

            } else if (format.equals("mp4")
                    || format.equals("mov")
                    || format.equals("avi")
                    || format.equals("mkv")
                    || format.equals("webm")) {

                optimizedSize
                        = videoOptimizationService.optimizeVideo(
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

            mediaRepository.save(media);

        } catch (Exception e) {

            media.setStatus("FAILED");
            mediaRepository.save(media);
        }
    }
}
