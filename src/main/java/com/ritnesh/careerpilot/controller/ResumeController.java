package com.ritnesh.careerpilot.controller;

import com.ritnesh.careerpilot.dto.PagedResponse;
import com.ritnesh.careerpilot.dto.JobMatchResponse;
import com.ritnesh.careerpilot.dto.ResumeAnalysisResponse;
import com.ritnesh.careerpilot.dto.ResumeSummaryResponse;
import com.ritnesh.careerpilot.dto.ResumeUploadResponse;
import com.ritnesh.careerpilot.entity.Resume;
import com.ritnesh.careerpilot.service.JobMatchingService;
import com.ritnesh.careerpilot.service.ResumeAnalysisService;
import com.ritnesh.careerpilot.service.ResumeService;

import java.util.List;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.URLEncoder;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    private final ResumeService resumeService;
    private final ResumeAnalysisService resumeAnalysisService;
    private final JobMatchingService jobMatchingService;

    public ResumeController(ResumeService resumeService,
                             ResumeAnalysisService resumeAnalysisService,
                             JobMatchingService jobMatchingService) {
        this.resumeService = resumeService;
        this.resumeAnalysisService = resumeAnalysisService;
        this.jobMatchingService = jobMatchingService;
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

    @GetMapping
    public ResponseEntity<PagedResponse<ResumeSummaryResponse>> listResumes(
            @PageableDefault(size = 10, sort = "uploadedAt") Pageable pageable,
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(resumeService.listResumes(email, pageable));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadResume(
            @PathVariable Long id,
            Authentication authentication
    ) throws IOException {

        String email = authentication.getName();

        Resume resume = resumeService.getDownloadableResume(id, email);
        Path filePath = Paths.get(resume.getFilePath());

        Resource resource = new FileSystemResource(filePath);

        String encodedFileName = URLEncoder.encode(resume.getOriginalFileName(), StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFileName)
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResume(
            @PathVariable Long id,
            Authentication authentication
    ) {

        String email = authentication.getName();

        resumeService.deleteResume(id, email);

        return ResponseEntity.noContent().build();
    }

    @PutMapping(
            value = "/{id}/replace",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<ResumeUploadResponse> replaceResume(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) throws IOException {

        String email = authentication.getName();

        ResumeUploadResponse response = resumeService.replaceResume(id, file, email);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/analyze")
    public ResponseEntity<ResumeAnalysisResponse> analyzeResume(
            @PathVariable Long id,
            Authentication authentication
    ) {

        String email = authentication.getName();

        // ownership + existence check is reused from ResumeService, not duplicated here
        Resume resume = resumeService.getDownloadableResume(id, email);

        ResumeAnalysisResponse response = resumeAnalysisService.startAnalysis(resume);

        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("/{id}/analysis")
    public ResponseEntity<ResumeAnalysisResponse> getAnalysis(
            @PathVariable Long id,
            Authentication authentication
    ) {

        String email = authentication.getName();

        // confirms the resume exists and belongs to this user before revealing any analysis
        resumeService.getDownloadableResume(id, email);

        return ResponseEntity.ok(resumeAnalysisService.getAnalysis(id));
    }

    @GetMapping("/{id}/job-matches")
    public ResponseEntity<List<JobMatchResponse>> getJobMatches(
            @PathVariable Long id,
            Authentication authentication
    ) {

        String email = authentication.getName();

        // confirms the resume exists and belongs to this user first
        resumeService.getDownloadableResume(id, email);

        return ResponseEntity.ok(jobMatchingService.getMatches(id));
    }
}
