package com.ritnesh.careerpilot.repository;

import com.ritnesh.careerpilot.entity.Resume;
import com.ritnesh.careerpilot.entity.ResumeAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Long> {

    Optional<ResumeAnalysis> findByResume(Resume resume);

    Optional<ResumeAnalysis> findByResumeId(Long resumeId);
}
