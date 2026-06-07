package com.smartgaon.ai.smartgaon_api.mediagallery.service;

import com.smartgaon.ai.smartgaon_api.exception.ResourceNotFoundException;
import com.smartgaon.ai.smartgaon_api.mediagallery.entity.MediaGallery;
import com.smartgaon.ai.smartgaon_api.mediagallery.repository.MediaGalleryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MediaGalleryService {

    private final MediaGalleryRepository mediaGalleryRepository;

    // Pagination (default 5 records per page from controller)
    public Page<MediaGallery> getAllActiveMedia(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return mediaGalleryRepository.findByActiveTrue(pageable);
    }

    public MediaGallery getActiveMediaById(Long id) {

        MediaGallery media = mediaGalleryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Media not found"));

        if (!Boolean.TRUE.equals(media.getActive())) {
            throw new ResourceNotFoundException("Media not found");
        }

        return media;
    }

    public List<MediaGallery> getByCategory(String category) {
        return mediaGalleryRepository.findByCategoryAndActiveTrue(category);
    }

    public List<MediaGallery> getByMediaType(String mediaType) {
        return mediaGalleryRepository.findByMediaTypeAndActiveTrue(mediaType);
    }

    public List<MediaGallery> searchByTitle(String title) {
        return mediaGalleryRepository
                .findByTitleContainingIgnoreCaseAndActiveTrue(title);
    }
}