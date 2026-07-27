package com.ritnesh.careerpilot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class JobMatchResponse {

    private Long jobId;
    private String title;
    private String company;
    private String location;
    private int matchScore; // 0-100
    private List<String> matchedSkills;
    private List<String> missingSkills;
}
