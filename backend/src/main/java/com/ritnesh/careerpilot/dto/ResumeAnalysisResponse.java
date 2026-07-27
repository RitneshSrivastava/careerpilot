package com.ritnesh.careerpilot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ResumeAnalysisResponse {

    private Long resumeId;

    private String status; // PENDING, COMPLETED, FAILED

    private Integer atsScore;

    private List<String> extractedSkills;

    private List<String> suggestions;

    private String summary;

    private String failureReason;

    private LocalDateTime analyzedAt;
}
