package com.smartgaon.ai.smartgaon_api.gaontalent.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class TalentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long entryId;
    private Long userId;
}


