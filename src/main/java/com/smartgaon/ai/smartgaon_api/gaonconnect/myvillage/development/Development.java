package com.smartgaon.ai.smartgaon_api.gaonconnect.myvillage.development;

//import jakarta.persistence.*;
//import lombok.*;
//import java.time.LocalDate;
//
//@Entity
//@Getter
//@Setter
//@Table(name = "development_phase")
//public class Development {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private Integer phaseNumber;
//
//    private String title;
//
//    @Column(length = 3000)
//    private String description;
//
//    @Enumerated(EnumType.STRING)
//    private PhaseStatus status;
//
//    private LocalDate startDate;
//
//    private LocalDate endDate;
//}
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "development_phase")
public class Development {

	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    private Integer phaseNumber;

	    @ManyToOne
	    @JoinColumn(name = "master_id")
	    private DevelopmentMaster master;

	    @Column(length = 3000)
	    private String description;

	    @Enumerated(EnumType.STRING)
	    private PhaseStatus status;

	    private LocalDate startDate;

	    private LocalDate endDate;
	    
	    @OneToMany(mappedBy = "development",
	            cascade = CascadeType.ALL,
	            orphanRemoval = true,
	            fetch = FetchType.EAGER)
	    private List<DevelopmentImage> images;
}