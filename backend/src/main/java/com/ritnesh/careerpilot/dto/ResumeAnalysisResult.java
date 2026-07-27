package com.ritnesh.careerpilot.dto;

import java.util.List;

/**
 * Structured output shape requested from the AI model.
 * Spring AI's BeanOutputConverter uses this class's fields (via reflection)
 * to build the format instructions and parse the model's JSON response.
 */
public class ResumeAnalysisResult {

    private int atsScore;
    private List<String> extractedSkills;
    private List<String> suggestions;
    private String summary;

    public ResumeAnalysisResult() {
    }

    public int getAtsScore() {
        return atsScore;
    }

    public void setAtsScore(int atsScore) {
        this.atsScore = atsScore;
    }

    public List<String> getExtractedSkills() {
        return extractedSkills;
    }

    public void setExtractedSkills(List<String> extractedSkills) {
        this.extractedSkills = extractedSkills;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
