//package com.smartgaon.ai.smartgaon_api.gaontalent.Service;
//
//import com.smartgaon.ai.smartgaon_api.auth.repository.UserRepository;
//import com.smartgaon.ai.smartgaon_api.cloudinary.CloudinaryService;
//import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.TalentVideo;
//import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.TalentVideoComment;
//import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.TalentVideoLike;
//import com.smartgaon.ai.smartgaon_api.gaontalent.Repository.TalentVideoCommentRepository;
//import com.smartgaon.ai.smartgaon_api.gaontalent.Repository.TalentVideoLikeRepository;
//import com.smartgaon.ai.smartgaon_api.gaontalent.Repository.TalentVideoRepository;
//import com.smartgaon.ai.smartgaon_api.model.User;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//import java.util.Optional;
//
//@Service
//@RequiredArgsConstructor
//public class TalentVideoService {
//
//    private final CloudinaryService cloudinaryService;
//    private final TalentVideoRepository repository;
//    private final UserRepository userRepository;
//    private final TalentVideoLikeRepository likeRepository;
//    private final TalentVideoCommentRepository commentRepository;
//
//
//    private final List<String> allowedFormats = List.of("mp4", "mov", "avi");
//
//    // -------------------------------------------------------
//    // 📌 UPLOAD VIDEO
//    // -------------------------------------------------------
//    public TalentVideo uploadVideo(MultipartFile file,
//                                   String title,
//                                   Long userId,
//                                   boolean isReel) throws Exception {
//
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new Exception("User not found"));
//
//        String username = user.getFirstName() + " " + user.getLastName();
//
//        String ext = file.getOriginalFilename()
//                .substring(file.getOriginalFilename().lastIndexOf(".") + 1)
//                .toLowerCase();
//
//        if (!allowedFormats.contains(ext)) {
//            throw new Exception("Allowed formats: MP4, MOV, AVI");
//        }
//
//        long maxSize = isReel ? 50L * 1024 * 1024 : 200L * 1024 * 1024;
//        if (file.getSize() > maxSize) {
//            throw new Exception("File size exceeded");
//        }
//
//        String url = cloudinaryService.uploadFile(file);
//
//        TalentVideo video = new TalentVideo();
//        video.setTitle(title);
//        video.setUserId(userId);
//        video.setUsername(username);
//        video.setUrl(url);
//        video.setReel(isReel);
//        video.setFormat(ext);
//        video.setFileSize(file.getSize());
//
//        return repository.save(video);
//    }
//
//    // -------------------------------------------------------
//    // 📌 ONE USER = ONE LIKE
//    // -------------------------------------------------------
//    public String likeVideo(String videoId, Long userId) {
//
//        Optional<TalentVideoLike> already =
//                likeRepository.findByUserIdAndVideoId(userId, videoId);
//
//        if (already.isPresent()) {
//            return "User already liked this video";
//        }
//
//        // Save new like
//        TalentVideoLike like = new TalentVideoLike();
//        like.setUserId(userId);
//        like.setVideoId(videoId);
//        likeRepository.save(like);
//
//        // Update like count
//        TalentVideo video = repository.findById(videoId)
//                .orElseThrow(() -> new RuntimeException("Video not found"));
//
//        int totalLikes = likeRepository.countByVideoId(videoId);
//        video.setLikes(totalLikes);
//        repository.save(video);
//
//        return "Liked successfully";
//    }
//
//    public String unlikeVideo(String videoId, Long userId) {
//
//        Optional<TalentVideoLike> existing =
//                likeRepository.findByUserIdAndVideoId(userId, videoId);
//
//        if (existing.isEmpty()) {
//            return "User has not liked this video";
//        }
//
//        // ✅ remove like record
//        likeRepository.delete(existing.get());
//
//        // ✅ update like count
//        TalentVideo video = repository.findById(videoId)
//                .orElseThrow(() -> new RuntimeException("Video not found"));
//
//        int totalLikes = likeRepository.countByVideoId(videoId);
//        video.setLikes(totalLikes);
//        repository.save(video);
//
//        return "Unliked successfully";
//    }
//
//    // -------------------------------------------------------
//    // 📌 COMMENT
//    // -------------------------------------------------------
////    public void commentVideo(String id) {
////        TalentVideo v = repository.findById(id)
////                .orElseThrow(() -> new RuntimeException("Video not found"));
////        v.setComments(v.getComments() + 1);
////        repository.save(v);
////    }
//    
//    public TalentVideoComment addComment(String videoId, Long userId, String text) {
//
//        TalentVideo video = repository.findById(videoId)
//                .orElseThrow(() -> new RuntimeException("Video not found"));
//
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        TalentVideoComment c = new TalentVideoComment();
//        c.setVideoId(videoId);
//        c.setUserId(userId);
//        c.setUsername(user.getFirstName() + " " + user.getLastName());
//        c.setText(text);
//
//        commentRepository.save(c);
//
//        video.setComments(video.getComments() + 1);
//        repository.save(video);
//
//        return c;
//    }
//
//
//    // -------------------------------------------------------
//    // 📌 SHARE
//    // -------------------------------------------------------
//    public void shareVideo(String id) {
//        TalentVideo v = repository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Video not found"));
//        v.setShares(v.getShares() + 1);
//        repository.save(v);
//    }
//
//    // -------------------------------------------------------
//    // 📌 PAGINATION FEED
//    // -------------------------------------------------------
//    public Page<TalentVideo> getPaginatedVideos(int page, int size) {
//        Pageable pageable = PageRequest.of(page, size, Sort.by("uploadedAt").descending());
//        return repository.findAll(pageable);
//    }
//}

package com.smartgaon.ai.smartgaon_api.gaontalent.Service;

import com.smartgaon.ai.smartgaon_api.auth.repository.UserRepository;
import com.smartgaon.ai.smartgaon_api.cloudinary.CloudinaryService;
import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.TalentVideo;
import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.TalentVideoLike;
import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.TalentVideoComment;
import com.smartgaon.ai.smartgaon_api.gaontalent.Repository.TalentVideoCommentRepository;
import com.smartgaon.ai.smartgaon_api.gaontalent.Repository.TalentVideoLikeRepository;
import com.smartgaon.ai.smartgaon_api.gaontalent.Repository.TalentVideoRepository;
import com.smartgaon.ai.smartgaon_api.model.User;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TalentVideoService {

    private final CloudinaryService cloudinaryService;
    private final TalentVideoRepository repository;
    private final UserRepository userRepository;
    private final TalentVideoLikeRepository likeRepository;
    private final TalentVideoCommentRepository commentRepository;

    private final List<String> allowedFormats = List.of("mp4", "mov", "avi");

    // -------------------------------------------------------
    // 📌 UPLOAD VIDEO
    // -------------------------------------------------------
    public TalentVideo uploadVideo(MultipartFile file,
                                   String title,
                                   Long userId,
                                   boolean isReel) throws Exception {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        String username = user.getFirstName() + " " + user.getLastName();

        String ext = file.getOriginalFilename()
                .substring(file.getOriginalFilename().lastIndexOf(".") + 1)
                .toLowerCase();

        if (!allowedFormats.contains(ext)) {
            throw new Exception("Allowed formats: MP4, MOV, AVI");
        }

        long maxSize = isReel ? 50L * 1024 * 1024 : 200L * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new Exception("File size exceeded");
        }

        String url = cloudinaryService.uploadFile(file);

        TalentVideo video = new TalentVideo();
        video.setTitle(title);
        video.setUserId(userId);
        video.setUsername(username);
        video.setUrl(url);
        video.setReel(isReel);
        video.setFormat(ext);
        video.setFileSize(file.getSize());

        return repository.save(video);
    }

    // -------------------------------------------------------
    // 📌 LIKE
    // -------------------------------------------------------
    public String likeVideo(String videoId, Long userId) {

        Optional<TalentVideoLike> already =
                likeRepository.findByUserIdAndVideoId(userId, videoId);

        if (already.isPresent()) {
            return "User already liked this video";
        }

        TalentVideoLike like = new TalentVideoLike();
        like.setUserId(userId);
        like.setVideoId(videoId);
        likeRepository.save(like);

        TalentVideo video = repository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        int totalLikes = likeRepository.countByVideoId(videoId);
        video.setLikes(totalLikes);
        repository.save(video);

        return "Liked successfully";
    }

    // -------------------------------------------------------
    // 📌 UNLIKE
    // -------------------------------------------------------
    public String unlikeVideo(String videoId, Long userId) {

        Optional<TalentVideoLike> existing =
                likeRepository.findByUserIdAndVideoId(userId, videoId);

        if (existing.isEmpty()) {
            return "User has not liked this video";
        }

        likeRepository.delete(existing.get());

        TalentVideo video = repository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        int totalLikes = likeRepository.countByVideoId(videoId);
        video.setLikes(totalLikes);
        repository.save(video);

        return "Unliked successfully";
    }

    // -------------------------------------------------------
    // 📌 ADD COMMENT (save text + user)
    // -------------------------------------------------------
    public TalentVideoComment addComment(String videoId, Long userId, String text) {

        TalentVideo video = repository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TalentVideoComment c = new TalentVideoComment();
        c.setVideoId(videoId);
        c.setUserId(userId);
        c.setUsername(user.getFirstName() + " " + user.getLastName());
        c.setText(text);

        commentRepository.save(c);

        video.setComments(video.getComments() + 1);
        repository.save(video);

        return c;
    }

    // -------------------------------------------------------
    // 📌 GET COMMENTS
    // -------------------------------------------------------
    public List<TalentVideoComment> getComments(String videoId) {
        return commentRepository.findByVideoIdOrderByCreatedAtDesc(videoId);
    }

    // -------------------------------------------------------
    // 📌 SHARE
    // -------------------------------------------------------
    public void shareVideo(String id) {
        TalentVideo v = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found"));
        v.setShares(v.getShares() + 1);
        repository.save(v);
    }

    // -------------------------------------------------------
    // 📌 PAGINATION FEED
    // -------------------------------------------------------
    public Page<TalentVideo> getPaginatedVideos(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("uploadedAt").descending());
        return repository.findAll(pageable);
    }
}

