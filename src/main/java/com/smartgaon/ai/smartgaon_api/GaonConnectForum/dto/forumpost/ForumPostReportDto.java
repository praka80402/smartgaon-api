package com.smartgaon.ai.smartgaon_api.GaonConnectForum.dto.forumpost;

import com.smartgaon.ai.smartgaon_api.GaonConnectForum.model.ReportReason;

public record ForumPostReportDto(
        Long postId,
        Long userId,
        ReportReason reason,
        String customReason
) {}

