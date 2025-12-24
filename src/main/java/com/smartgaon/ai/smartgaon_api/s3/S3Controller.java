package com.smartgaon.ai.smartgaon_api.s3;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/forum/media")
public class S3Controller {

    private final S3Service s3Service;

    public S3Controller(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    /** SAME endpoint as Cloudinary */
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadMedia(@RequestParam("file") MultipartFile file) {
        String url = s3Service.uploadFile(file);
        return ResponseEntity.ok(url);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Boolean> deleteMedia(@RequestParam String url) {
        return ResponseEntity.ok(s3Service.deleteFile(url));
    }
}
