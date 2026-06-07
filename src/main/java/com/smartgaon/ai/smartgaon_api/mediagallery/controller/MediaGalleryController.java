package com.smartgaon.ai.smartgaon_api.mediagallery.controller;

import com.smartgaon.ai.smartgaon_api.mediagallery.entity.MediaGallery;
import com.smartgaon.ai.smartgaon_api.mediagallery.service.MediaGalleryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/media-gallery")
@RequiredArgsConstructor
public class MediaGalleryController {

    private final MediaGalleryService mediaGalleryService;

    @GetMapping
    public Page<MediaGallery> getAllMedia(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        return mediaGalleryService.getAllActiveMedia(page, size);
    }

    @GetMapping("/{id}")
    public MediaGallery getMediaById(@PathVariable Long id) {
        return mediaGalleryService.getActiveMediaById(id);
    }

    @GetMapping("/category/{category}")
    public List<MediaGallery> getByCategory(@PathVariable String category) {
        return mediaGalleryService.getByCategory(category);
    }

    @GetMapping("/media-type/{mediaType}")
    public List<MediaGallery> getByMediaType(@PathVariable String mediaType) {
        return mediaGalleryService.getByMediaType(mediaType);
    }

    @GetMapping("/search")
    public List<MediaGallery> searchByTitle(
            @RequestParam String title) {
        return mediaGalleryService.searchByTitle(title);
    }
}