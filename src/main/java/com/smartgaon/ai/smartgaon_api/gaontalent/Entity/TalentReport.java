package com.smartgaon.ai.smartgaon_api.gaontalent.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"entryId", "userId"})
})
public class TalentReport {

	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    private Long entryId;
	    private Long userId;

	    @Enumerated(EnumType.STRING)
	    private TalentReportReason reason;

	    @Column(columnDefinition = "TEXT")
	    private String customReason;   // only when reason = OTHER

	    private LocalDateTime reportedAt = LocalDateTime.now();
}

