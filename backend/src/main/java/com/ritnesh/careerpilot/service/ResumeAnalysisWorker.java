package com.ritnesh.careerpilot.service;

import com.ritnesh.careerpilot.dto.ResumeAnalysisResult;
import com.ritnesh.careerpilot.entity.ResumeAnalysis;
import com.ritnesh.careerpilot.repository.ResumeAnalysisRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * Runs the actual PDF-parsing + AI call off the request thread.
 *
 * NOTE ON DESIGN: this is a separate bean (not a private method on
 * ResumeAnalysisService) on purpose. Spring's @Async works via a proxy
 * wrapping the bean — calling an @Async method from another method on the
 * SAME bean bypasses that proxy entirely and runs synchronously, silently.
 * Splitting the async work into its own bean avoids that self-invocation trap.
 */
@Service
public class ResumeAnalysisWorker {

    private final ChatClient chatClient;
    private final PdfTextExtractionService pdfTextExtractionService;
    private final ResumeAnalysisRepository analysisRepository;

    public ResumeAnalysisWorker(ChatClient.Builder chatClientBuilder,
                                 PdfTextExtractionService pdfTextExtractionService,
                                 ResumeAnalysisRepository analysisRepository) {

        this.chatClient = chatClientBuilder.build();
        this.pdfTextExtractionService = pdfTextExtractionService;
        this.analysisRepository = analysisRepository;
    }

    private static final String SYSTEM_PROMPT = """
            You are an expert technical resume reviewer and ATS (Applicant Tracking System)
            simulator. You will be given the raw extracted text of a candidate's resume.

            Analyze it and return:
            - atsScore: an integer 0-100 estimating how well this resume would pass a typical
              ATS parser and recruiter skim (formatting clarity, keyword density, quantified impact).
            - extractedSkills: a list of concrete technical and professional skills you can find
              explicitly stated or very strongly implied in the resume.
            - suggestions: a list of specific, actionable improvements (not generic advice).
              Reference actual content from the resume where possible.
            - summary: a 2-3 sentence overall assessment.

            Be honest and specific. Do not inflate the score.
            """;

    @Async
    public void analyze(Long analysisId, Path resumeFilePath) {

        ResumeAnalysis analysis = analysisRepository.findById(analysisId).orElse(null);
        if (analysis == null) {
            return; // record was deleted/changed before the worker ran; nothing to do
        }

        try {
            String resumeText = pdfTextExtractionService.extractText(resumeFilePath);

            if (resumeText.isBlank()) {
                markFailed(analysis, "Could not extract any text from this PDF. " +
                        "It may be a scanned image rather than a text-based PDF.");
                return;
            }

            ResumeAnalysisResult result = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(resumeText)
                    .call()
                    .entity(ResumeAnalysisResult.class);

            if (result == null) {
                markFailed(analysis, "AI model returned no result.");
                return;
            }

            analysis.setStatus(ResumeAnalysis.AnalysisStatus.COMPLETED);
            analysis.setAtsScore(result.getAtsScore());
            analysis.setExtractedSkillsCsv(String.join(",", safeList(result.getExtractedSkills())));
            analysis.setSuggestionsText(String.join("\n", safeList(result.getSuggestions())));
            analysis.setSummary(result.getSummary());
            analysis.setFailureReason(null);
            analysis.setAnalyzedAt(LocalDateTime.now());

            analysisRepository.save(analysis);

        } catch (Exception ex) {
            // Broad catch is intentional here: this runs on a background thread with no
            // caller waiting on it, so any failure (AI API error, malformed PDF, etc.)
            // must be persisted as a FAILED status rather than lost in a background thread.
            markFailed(analysis, "Analysis failed: " + ex.getMessage());
        }
    }

    private void markFailed(ResumeAnalysis analysis, String reason) {
        analysis.setStatus(ResumeAnalysis.AnalysisStatus.FAILED);
        analysis.setFailureReason(reason);
        analysis.setAnalyzedAt(LocalDateTime.now());
        analysisRepository.save(analysis);
    }

    private java.util.List<String> safeList(java.util.List<String> list) {
        return list == null ? java.util.List.of() : list;
    }
}
