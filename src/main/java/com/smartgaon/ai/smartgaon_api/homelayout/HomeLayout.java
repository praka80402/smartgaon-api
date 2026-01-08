package com.smartgaon.ai.smartgaon_api.homelayout;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "home_layout")
@Getter
@Setter
public class HomeLayout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String screen;
    private String platform;

    @OneToMany(
            mappedBy = "layout",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("displayOrder ASC")
    private List<HomeLayoutSection> sections = new ArrayList<>();
}
