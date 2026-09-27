package com.example.mediaforge.repository;

import com.example.mediaforge.entity.Media;
import org.springframework.data.jpa.domain.Specification;

public class MediaSpecification {

    public static Specification<Media> belongsToUser(String username) {
        return (root, query, cb)
                -> cb.equal(root.get("user").get("username"), username);
    }

    public static Specification<Media> filenameContains(String filename) {
        return (root, query, cb)
                -> cb.like(
                        cb.lower(root.get("filename")),
                        "%" + filename.toLowerCase() + "%"
                );
    }

    public static Specification<Media> formatEquals(String format) {
        return (root, query, cb)
                -> cb.equal(
                        cb.lower(root.get("format")),
                        format.toLowerCase()
                );
    }

    public static Specification<Media> statusEquals(String status) {
        return (root, query, cb)
                -> cb.equal(
                        cb.lower(root.get("status")),
                        status.toLowerCase()
                );
    }
}
