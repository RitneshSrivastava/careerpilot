package com.ritnesh.careerpilot.service;

import com.ritnesh.careerpilot.dto.ResumeUploadResponse;
import com.ritnesh.careerpilot.entity.Resume;
import com.ritnesh.careerpilot.entity.User;
import com.ritnesh.careerpilot.repository.ResumeRepository;
import com.ritnesh.careerpilot.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;

    private static final String UPLOAD_DIR = "uploads";

    public ResumeService(ResumeRepository resumeRepository,
                         UserRepository userRepository) {

        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
    }

    public ResumeUploadResponse uploadResume(MultipartFile file, String email)
            throws IOException {

        if (file.isEmpty()) {
            throw new RuntimeException("Please select a file.");
        }

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null ||
                !originalFileName.toLowerCase().endsWith(".pdf")) {

            throw new RuntimeException("Only PDF files are allowed.");
        }

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            throw new RuntimeException("User not found.");
        }

        User user = optionalUser.get();

        Path uploadPath = Paths.get(UPLOAD_DIR);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String storedFileName =
                UUID.randomUUID() + "_" + originalFileName;

        Path filePath = uploadPath.resolve(storedFileName);

        Files.copy(
                file.getInputStream(),
                filePath,
                StandardCopyOption.REPLACE_EXISTING
        );

        Resume resume = new Resume();

        resume.setOriginalFileName(originalFileName);
        resume.setStoredFileName(storedFileName);
        resume.setFilePath(filePath.toString());
        resume.setUploadedAt(LocalDateTime.now());
        resume.setUser(user);

        resumeRepository.save(resume);

        return new ResumeUploadResponse(
                "Resume uploaded successfully.",
                originalFileName
        );
    }
}