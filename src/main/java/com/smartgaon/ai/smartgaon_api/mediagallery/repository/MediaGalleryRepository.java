package com.smartgaon.ai.smartgaon_api.mediagallery.repository;

import com.smartgaon.ai.smartgaon_api.mediagallery.entity.MediaGallery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaGalleryRepository extends JpaRepository<MediaGallery, Long> {

    // Pagination
    Page<MediaGallery> findByActiveTrue(Pageable pageable);

    // Category Filter
    List<MediaGallery> findByCategoryAndActiveTrue(String category);

    // Media Type Filter
    List<MediaGallery> findByMediaTypeAndActiveTrue(String mediaType);

    // Search by Title
    List<MediaGallery> findByTitleContainingIgnoreCaseAndActiveTrue(String title);
}