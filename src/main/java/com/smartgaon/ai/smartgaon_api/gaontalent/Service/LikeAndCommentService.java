package com.smartgaon.ai.smartgaon_api.gaontalent.Service;

import lombok.RequiredArgsConstructor;

import java.util.List;



import org.springframework.stereotype.Service;

import com.smartgaon.ai.smartgaon_api.auth.repository.UserRepository;
import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.TalentComment;
import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.TalentEntry;
import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.TalentLike;
import com.smartgaon.ai.smartgaon_api.gaontalent.Repository.TalentCommentRepository;
import com.smartgaon.ai.smartgaon_api.gaontalent.Repository.TalentEntryRepository;
import com.smartgaon.ai.smartgaon_api.gaontalent.Repository.TalentLikeRepository;
import com.smartgaon.ai.smartgaon_api.model.User;

@Service
@RequiredArgsConstructor
public class LikeAndCommentService {

    private final TalentLikeRepository likeRepo;
    private final TalentCommentRepository commentRepo;
    private final TalentEntryRepository entryRepo;
    private final UserRepository userRepo;

    /* =========================
            LIKE TOGGLE
       ========================= */
    public String addLike(Long entryId, Long userId) {

        // Check if user already liked this entry
        boolean exists = likeRepo.existsByEntryIdAndUserId(entryId, userId);
        if (exists) {
            return "Already liked";
        }

        // Save like
        TalentLike like = new TalentLike();
        like.setEntryId(entryId);
        like.setUserId(userId);
        likeRepo.save(like);

        // Increase like count
        TalentEntry entry = entryRepo.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Entry not found"));

        entry.setLikes(entry.getLikes() + 1);
        entryRepo.save(entry);

        return "Likes updated: " + entry.getLikes();
    }
    /* =========================
             ADD COMMENT
       ========================= */
    public TalentComment addComment(Long entryId, Long userId, String text) {

        TalentEntry entry = entryRepo.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Entry not found"));

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TalentComment comment = new TalentComment();
        comment.setEntryId(entryId);
        comment.setUserId(userId);
        comment.setUsername(user.getFirstName() + " " + user.getLastName());
        comment.setText(text);

        commentRepo.save(comment);

        // Update comment count
        int count = commentRepo.countByEntryId(entryId);
        entry.setComments(count);
        entryRepo.save(entry);

        return comment;
    }

    /* =========================
             GET COMMENTS
       ========================= */
    public List<TalentComment> getComments(Long entryId) {
        return commentRepo.findByEntryIdOrderByCreatedAtDesc(entryId);
    }
    /* =========================
    GET LIKE STATUS
========================= */
public Object getLikes(Long entryId, Long userId) {

int likeCount = likeRepo.countByEntryId(entryId);

boolean liked = likeRepo.existsByEntryIdAndUserId(entryId, userId);

return new java.util.HashMap<String, Object>() {{
   put("likes", likeCount);
   put("liked", liked);
}};
}

}
