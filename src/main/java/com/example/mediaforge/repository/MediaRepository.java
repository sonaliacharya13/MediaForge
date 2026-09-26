package com.example.mediaforge.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mediaforge.entity.Media;

public interface MediaRepository extends JpaRepository<Media, Long> {

    Page<Media> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
