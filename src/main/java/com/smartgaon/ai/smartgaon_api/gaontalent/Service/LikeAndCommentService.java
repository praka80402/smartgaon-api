package com.smartgaon.ai.smartgaon_api.gaontalent.Service;

import lombok.RequiredArgsConstructor;
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
          LIKE (TOGGLE)
       ========================= */
    public String like(Long entryId, Long userId) {

        TalentEntry entry = entryRepo.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Entry not found"));

        var existing = likeRepo.findByEntryIdAndUserId(entryId, userId);

        if (existing.isPresent()) {
            // Unlike
            likeRepo.delete(existing.get());
        } else {
            // Add like
            TalentLike like = new TalentLike();
            like.setEntryId(entryId);
            like.setUserId(userId);
            likeRepo.save(like);
        }

        // Update Like Count
        int likeCount = likeRepo.countByEntryId(entryId);
        entry.setLikes(likeCount);
        entryRepo.save(entry);

        return "Likes updated: " + likeCount;
    }

    /* =========================
           ADD COMMENT
       ========================= */
    public TalentComment addComment(Long entryId, Long userId, String text) {

        TalentEntry entry = entryRepo.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Entry not found"));

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TalentComment c = new TalentComment();
        c.setEntryId(entryId);
        c.setUserId(userId);
        c.setUsername(user.getFirstName() + " " + user.getLastName());
        c.setText(text);
        commentRepo.save(c);

        // Update Comment Count
        int count = commentRepo.countByEntryId(entryId);
        entry.setComments(count);
        entryRepo.save(entry);

        return c;
    }
}
