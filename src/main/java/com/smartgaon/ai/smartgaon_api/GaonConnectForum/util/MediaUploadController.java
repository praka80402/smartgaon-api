//package com.smartgaon.ai.smartgaon_api.GaonConnectForum.util;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/forum/media")
//public class MediaUploadController {
//
//    @Autowired
//    private MediaUploadService mediaUploadService;
//
//    // Upload single file
//    @PostMapping("/upload")
//    public String uploadSingle(@RequestParam("file") MultipartFile file) {
//        return mediaUploadService.uploadFile(file);
//    }
//
//    // Upload multiple files
//    @PostMapping("/upload-multiple")
//    public List<String> uploadMultiple(@RequestParam("files") MultipartFile[] files) {
//        return mediaUploadService.uploadFiles(files);
//    }
//}
