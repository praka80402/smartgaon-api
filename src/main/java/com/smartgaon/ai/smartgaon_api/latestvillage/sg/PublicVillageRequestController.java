package com.smartgaon.ai.smartgaon_api.latestvillage.sg;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

/**
 * Public endpoints used by the website's "Create your village" dialog.
 *
 *  POST /api/public/village-requests            → submit the form
 *  GET  /api/public/village-requests/files/**   → streams an image from the
 *       PRIVATE S3 bucket (this is the URL stored in the DB, so images
 *       render everywhere without any public bucket policy).
 */
@RestController
@RequestMapping("/api/public/village-requests")
@CrossOrigin
public class PublicVillageRequestController {

    private final VillageRequestSubmitService service;

    public PublicVillageRequestController(VillageRequestSubmitService service) {
        this.service = service;
    }

    /** Submit a new village request (multipart: data + images + placePhoto_i). */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> submit(MultipartHttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.submit(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace(); // real cause (e.g. S3 credentials) in the console
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "Could not submit request"));
        }
    }

    /**
     * Streams an image from the private S3 bucket.
     * Example: GET /api/public/village-requests/files/village-requests/images/ab12.jpg
     */
    @GetMapping("/files/**")
    public ResponseEntity<?> serveFile(HttpServletRequest request) {
        // Everything after ".../files/" is the S3 object key
        String uri = request.getRequestURI();
        String marker = "/files/";
        int idx = uri.indexOf(marker);
        if (idx < 0) return ResponseEntity.notFound().build();
        String key = uri.substring(idx + marker.length());

        try {
            ResponseBytes<GetObjectResponse> obj = service.readObject(key);
            String contentType = obj.response().contentType();
            MediaType mediaType = (contentType != null && !contentType.isBlank())
                    ? MediaType.parseMediaType(contentType)
                    : MediaType.IMAGE_JPEG;
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                    .body(obj.asByteArray());
        } catch (NoSuchKeyException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}