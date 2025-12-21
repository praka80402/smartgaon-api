package com.smartgaon.ai.smartgaon_api.gaontalent.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class TalentWinner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long entryId;

    private LocalDateTime declaredAt = LocalDateTime.now();
}

