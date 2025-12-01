package com.smartgaon.ai.smartgaon_api.cloudinary;



import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public String uploadFile(MultipartFile file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("resource_type", "auto"));
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Image upload failed", e);
        }
    }
    
    
    
    
    public boolean deleteFile(String urlOrPublicId) {
        try {
            String publicId = toPublicId(urlOrPublicId);
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            Object res = result.get("result");
            return res != null && "ok".equals(res.toString());
        } catch (Exception e) {
           
            throw new RuntimeException("Cloudinary delete failed: " + e.getMessage(), e);
        }
    }

    
    private String toPublicId(String urlOrPublicId) {
        if (urlOrPublicId == null) return "";
        String s = urlOrPublicId;

       
        int q = s.indexOf('?');
        if (q >= 0) s = s.substring(0, q);

      
        int uploadIdx = s.indexOf("/upload/");
        if (uploadIdx >= 0) {
            s = s.substring(uploadIdx + 8); 
        }

       
        s = s.replaceFirst("^v[0-9]+/", "");

       
        int lastDot = s.lastIndexOf('.');
        if (lastDot > -1) {
            s = s.substring(0, lastDot);
        }

        
        if (s.startsWith("/")) s = s.substring(1);

        return s;
    }
}


