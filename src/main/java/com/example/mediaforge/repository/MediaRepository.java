package com.example.mediaforge.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mediaforge.entity.Media;

public interface MediaRepository extends JpaRepository<Media, Long> {

    Page<Media> findByUserUsernameOrderByCreatedAtDesc(
            String username,
            Pageable pageable
    );

    Optional<Media> findByIdAndUserUsername(
            Long id,
            String username
    );

    Page<Media> findByUserUsernameAndStatusIgnoreCase(
            String username,
            String status,
            Pageable pageable
    );

    Page<Media> findByUserUsernameAndFormatIgnoreCase(
            String username,
            String format,
            Pageable pageable
    );

    Page<Media> findByUserUsernameAndFilenameContainingIgnoreCase(
            String username,
            String filename,
            Pageable pageable
    );
}
