//package com.smartgaon.ai.smartgaon_api.GaonConnectForum.util;
//
//
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.*;
//import java.util.*;
//
//@Service
//public class MediaUploadService {
//
//    private final String UPLOAD_DIR = "src/main/resources/static/uploads/";
//
//    // Allowed file extensions
//    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png");
//    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4");
//
//    // File size limits
//    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;    // 5 MB
//    private static final long MAX_VIDEO_SIZE = 20 * 1024 * 1024;  // 20 MB
//
//    public MediaUploadService() {
//        File directory = new File(UPLOAD_DIR);
//        if (!directory.exists()) {
//            directory.mkdirs();
//        }
//    }
//
//    public String uploadFile(MultipartFile file) {
//
//        validateFile(file);
//
//        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
//        Path filePath = Paths.get(UPLOAD_DIR + fileName);
//
//        try {
//            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
//        } catch (IOException e) {
//            throw new RuntimeException("File upload failed: " + e.getMessage());
//        }
//
//        return "/uploads/" + fileName;
//    }
//
//    public List<String> uploadFiles(MultipartFile[] files) {
//        List<String> urls = new ArrayList<>();
//
//        for (MultipartFile file : files) {
//            urls.add(uploadFile(file));
//        }
//
//        return urls;
//    }
//
//    // ----------------------------------------
//    // VALIDATION LOGIC BELOW
//    // ----------------------------------------
//    private void validateFile(MultipartFile file) {
//
//        if (file.isEmpty()) {
//            throw new RuntimeException("File is empty");
//        }
//
//        String originalName = file.getOriginalFilename();
//        if (originalName == null) {
//            throw new RuntimeException("Invalid file name");
//        }
//
//        String extension = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
//
//        // Check allowed extensions
//        boolean isImage = IMAGE_EXTENSIONS.contains(extension);
//        boolean isVideo = VIDEO_EXTENSIONS.contains(extension);
//
//        if (!isImage && !isVideo) {
//            throw new RuntimeException(
//                    "Invalid file type. Allowed: JPG, JPEG, PNG, MP4"
//            );
//        }
//
//        // Check size limit
//        if (isImage && file.getSize() > MAX_IMAGE_SIZE) {
//            throw new RuntimeException("Image file too large! Max size: 5 MB");
//        }
//
//        if (isVideo && file.getSize() > MAX_VIDEO_SIZE) {
//            throw new RuntimeException("Video file too large! Max size: 20 MB");
//        }
//    }
//}
