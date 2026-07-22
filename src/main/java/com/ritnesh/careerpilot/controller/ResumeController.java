package com.ritnesh.careerpilot.controller;

import com.ritnesh.careerpilot.dto.ResumeUploadResponse;
import com.ritnesh.careerpilot.service.ResumeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping(
            value = "/upload",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<ResumeUploadResponse> uploadResume(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) throws IOException {

        String email = authentication.getName();

        ResumeUploadResponse response =
                resumeService.uploadResume(file, email);

        return ResponseEntity.ok(response);
    }
}