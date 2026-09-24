package com.example.mediaforge.repository;

import com.example.mediaforge.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaRepository extends JpaRepository<Media, Long> {
}
