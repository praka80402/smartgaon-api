// package com.smartgaon.ai.smartgaon_api.cloudinary;

// import com.smartgaon.ai.smartgaon_api.s3.S3Service;
// import org.springframework.stereotype.Service;
// import org.springframework.web.multipart.MultipartFile;

// @Service
// public class CloudinaryService {

//     private final S3Service s3Service;

//     // 🔁 Inject S3 instead of Cloudinary
//     public CloudinaryService(S3Service s3Service) {
//         this.s3Service = s3Service;
//     }

//     /**
//      * Adapter method
//      * Frontend still calls Cloudinary API,
//      * but files are uploaded to AWS S3.
//      */
//     public String uploadFile(MultipartFile file) {
//         return s3Service.uploadFile(file);
//     }

//     /**
//      * Adapter delete (optional)
//      * Redirect delete to S3
//      */
//     public boolean deleteFile(String url) {
//         return s3Service.deleteFile(url);
//     }
// }
