package com.ritnesh.careerpilot.controller;

import com.ritnesh.careerpilot.dto.JobListingResponse;
import com.ritnesh.careerpilot.dto.PagedResponse;
import com.ritnesh.careerpilot.service.JobMatchingService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobMatchingService jobMatchingService;

    public JobController(JobMatchingService jobMatchingService) {
        this.jobMatchingService = jobMatchingService;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<JobListingResponse>> listJobs(
            @PageableDefault(size = 10, sort = "postedAt") Pageable pageable
    ) {
        return ResponseEntity.ok(PagedResponse.from(jobMatchingService.listJobs(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobListingResponse> getJob(@PathVariable Long id) {
        return ResponseEntity.ok(jobMatchingService.getJob(id));
    }
}
