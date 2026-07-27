package com.ritnesh.careerpilot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ResumeSummaryResponse {

    private Long id;

    private String originalFileName;

    private long fileSizeBytes;

    private LocalDateTime uploadedAt;
}
