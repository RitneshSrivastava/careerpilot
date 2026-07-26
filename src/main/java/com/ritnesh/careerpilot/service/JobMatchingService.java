package com.ritnesh.careerpilot.service;

import com.ritnesh.careerpilot.dto.JobListingResponse;
import com.ritnesh.careerpilot.dto.JobMatchResponse;
import com.ritnesh.careerpilot.entity.JobListing;
import com.ritnesh.careerpilot.entity.ResumeAnalysis;
import com.ritnesh.careerpilot.exception.AnalysisNotFoundException;
import com.ritnesh.careerpilot.exception.JobNotFoundException;
import com.ritnesh.careerpilot.repository.JobListingRepository;
import com.ritnesh.careerpilot.repository.ResumeAnalysisRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class JobMatchingService {

    private final JobListingRepository jobListingRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;

    public JobMatchingService(JobListingRepository jobListingRepository,
                               ResumeAnalysisRepository resumeAnalysisRepository) {
        this.jobListingRepository = jobListingRepository;
        this.resumeAnalysisRepository = resumeAnalysisRepository;
    }

    public Page<JobListingResponse> listJobs(Pageable pageable) {
        return jobListingRepository.findAll(pageable)
                .map(this::toListingResponse);
    }

    public JobListingResponse getJob(Long jobId) {
        JobListing job = jobListingRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException("Job not found with id: " + jobId));
        return toListingResponse(job);
    }

    /**
     * Scores every job listing against the resume's AI-extracted skills.
     * Deliberately deterministic (skill-overlap based) rather than another
     * AI call: it's free, instant, and fully explainable - "why did I match
     * 60%" has a precise answer instead of a black-box model output.
     */
    public List<JobMatchResponse> getMatches(Long resumeId) {

        ResumeAnalysis analysis = resumeAnalysisRepository.findByResumeId(resumeId)
                .orElseThrow(() -> new AnalysisNotFoundException(
                        "No completed analysis for this resume yet. POST to /api/resume/" +
                                resumeId + "/analyze first, then try again once it's COMPLETED."));

        if (analysis.getStatus() != ResumeAnalysis.AnalysisStatus.COMPLETED) {
            throw new AnalysisNotFoundException(
                    "Resume analysis is not completed yet (status: " + analysis.getStatus() + ").");
        }

        Set<String> resumeSkills = toSkillSet(analysis.getExtractedSkillsCsv());

        List<JobListing> allJobs = jobListingRepository.findAll();

        List<JobMatchResponse> matches = allJobs.stream()
                .map(job -> scoreJob(job, resumeSkills))
                .sorted(Comparator.comparingInt(JobMatchResponse::getMatchScore).reversed())
                .collect(Collectors.toList());

        return matches;
    }

    private JobMatchResponse scoreJob(JobListing job, Set<String> resumeSkills) {

        List<String> requiredSkills = splitCsv(job.getRequiredSkillsCsv());

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String required : requiredSkills) {
            if (resumeSkills.contains(required.toLowerCase().trim())) {
                matched.add(required);
            } else {
                missing.add(required);
            }
        }

        int score = requiredSkills.isEmpty()
                ? 0
                : Math.round((matched.size() * 100f) / requiredSkills.size());

        return new JobMatchResponse(
                job.getId(),
                job.getTitle(),
                job.getCompany(),
                job.getLocation(),
                score,
                matched,
                missing
        );
    }

    private Set<String> toSkillSet(String csv) {
        return splitCsv(csv).stream()
                .map(s -> s.toLowerCase().trim())
                .collect(Collectors.toSet());
    }

    private List<String> splitCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private JobListingResponse toListingResponse(JobListing job) {
        return new JobListingResponse(
                job.getId(),
                job.getTitle(),
                job.getCompany(),
                job.getLocation(),
                job.getDescription(),
                splitCsv(job.getRequiredSkillsCsv()),
                job.getPostedAt()
        );
    }
}
