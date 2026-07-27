package com.ritnesh.careerpilot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class JobListingResponse {

    private Long id;
    private String title;
    private String company;
    private String location;
    private String description;
    private List<String> requiredSkills;
    private LocalDateTime postedAt;
}
