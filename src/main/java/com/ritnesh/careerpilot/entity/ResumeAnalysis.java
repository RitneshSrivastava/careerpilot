package com.ritnesh.careerpilot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "resume_analyses")
public class ResumeAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false, unique = true)
    private Resume resume;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnalysisStatus status;

    private Integer atsScore;

    @Lob
    private String extractedSkillsCsv;

    @Lob
    private String suggestionsText;

    @Lob
    private String summary;

    @Lob
    private String failureReason;

    private LocalDateTime analyzedAt;

    public enum AnalysisStatus {
        PENDING,
        COMPLETED,
        FAILED
    }
}
