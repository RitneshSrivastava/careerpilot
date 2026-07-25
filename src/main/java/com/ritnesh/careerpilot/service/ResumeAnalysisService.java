package com.ritnesh.careerpilot.service;

import com.ritnesh.careerpilot.dto.ResumeAnalysisResponse;
import com.ritnesh.careerpilot.entity.Resume;
import com.ritnesh.careerpilot.entity.ResumeAnalysis;
import com.ritnesh.careerpilot.exception.AnalysisNotFoundException;
import com.ritnesh.careerpilot.exception.AnalysisRateLimitException;
import com.ritnesh.careerpilot.repository.ResumeAnalysisRepository;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class ResumeAnalysisService {

    private final ResumeAnalysisRepository analysisRepository;
    private final ResumeAnalysisWorker worker;

    public ResumeAnalysisService(ResumeAnalysisRepository analysisRepository,
                                  ResumeAnalysisWorker worker) {
        this.analysisRepository = analysisRepository;
        this.worker = worker;
    }

    /**
     * Kicks off (or reuses) an analysis for the given resume.
     * Acts as its own cache: if a COMPLETED analysis already exists for the
     * CURRENT version of the file (nothing re-uploaded since), it's returned
     * immediately with no new AI call and no cost.
     */
    public ResumeAnalysisResponse startAnalysis(Resume resume) {

        Optional<ResumeAnalysis> existingOpt = analysisRepository.findByResumeId(resume.getId());

        if (existingOpt.isPresent()) {
            ResumeAnalysis existing = existingOpt.get();

            boolean completedForCurrentFile =
                    existing.getStatus() == ResumeAnalysis.AnalysisStatus.COMPLETED
                            && existing.getAnalyzedAt() != null
                            && !existing.getAnalyzedAt().isBefore(resume.getUploadedAt());

            if (completedForCurrentFile) {
                return toResponse(existing);
            }

            if (existing.getStatus() == ResumeAnalysis.AnalysisStatus.PENDING) {
                throw new AnalysisRateLimitException(
                        "An analysis is already in progress for this resume. Please wait for it to finish.");
            }
        }

        ResumeAnalysis analysis = existingOpt.orElseGet(ResumeAnalysis::new);
        analysis.setResume(resume);
        analysis.setStatus(ResumeAnalysis.AnalysisStatus.PENDING);
        analysis.setAnalyzedAt(LocalDateTime.now());
        analysis = analysisRepository.save(analysis);

        worker.analyze(analysis.getId(), Paths.get(resume.getFilePath()));

        return toResponse(analysis);
    }

    public ResumeAnalysisResponse getAnalysis(Long resumeId) {

        ResumeAnalysis analysis = analysisRepository.findByResumeId(resumeId)
                .orElseThrow(() -> new AnalysisNotFoundException(
                        "No analysis has been requested for this resume yet. POST to /api/resume/" + resumeId + "/analyze first."));

        return toResponse(analysis);
    }

    private ResumeAnalysisResponse toResponse(ResumeAnalysis analysis) {

        List<String> skills = splitOrEmpty(analysis.getExtractedSkillsCsv(), ",");
        List<String> suggestions = splitOrEmpty(analysis.getSuggestionsText(), "\n");

        return new ResumeAnalysisResponse(
                analysis.getResume().getId(),
                analysis.getStatus().name(),
                analysis.getAtsScore(),
                skills,
                suggestions,
                analysis.getSummary(),
                analysis.getFailureReason(),
                analysis.getAnalyzedAt()
        );
    }

    private List<String> splitOrEmpty(String value, String delimiter) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(delimiter))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
