package com.ritnesh.careerpilot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "job_listings")
public class JobListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String company;

    private String location;

    @Lob
    private String description;

    // Comma-separated, same convention as ResumeAnalysis.extractedSkillsCsv
    @Lob
    private String requiredSkillsCsv;

    private LocalDateTime postedAt;
}
