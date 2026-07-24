package com.ritnesh.careerpilot.service;

import com.ritnesh.careerpilot.dto.PagedResponse;
import com.ritnesh.careerpilot.dto.ResumeSummaryResponse;
import com.ritnesh.careerpilot.dto.ResumeUploadResponse;
import com.ritnesh.careerpilot.entity.Resume;
import com.ritnesh.careerpilot.entity.User;
import com.ritnesh.careerpilot.exception.InvalidFileException;
import com.ritnesh.careerpilot.exception.ResumeNotFoundException;
import com.ritnesh.careerpilot.exception.UnauthorizedResumeAccessException;
import com.ritnesh.careerpilot.repository.ResumeRepository;
import com.ritnesh.careerpilot.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024; // 5MB, matches application.properties

    public ResumeService(ResumeRepository resumeRepository,
                         UserRepository userRepository) {

        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
    }

    public ResumeUploadResponse uploadResume(MultipartFile file, String email)
            throws IOException {

        User user = getUserOrThrow(email);
        String originalFileName = validateAndSanitizeFile(file);

        Resume resume = new Resume();
        persistFileAndPopulateResume(resume, file, originalFileName);
        resume.setUser(user);

        resumeRepository.save(resume);

        return new ResumeUploadResponse(
                "Resume uploaded successfully.",
                originalFileName
        );
    }

    public PagedResponse<ResumeSummaryResponse> listResumes(String email, Pageable pageable) {

        User user = getUserOrThrow(email);

        Page<ResumeSummaryResponse> page = resumeRepository
                .findByUserAndDeletedFalse(user, pageable)
                .map(resume -> new ResumeSummaryResponse(
                        resume.getId(),
                        resume.getOriginalFileName(),
                        resume.getFileSizeBytes(),
                        resume.getUploadedAt()
                ));

        return PagedResponse.from(page);
    }

    public Resume getDownloadableResume(Long resumeId, String email) {
        return getOwnedResumeOrThrow(resumeId, email);
    }

    public void deleteResume(Long resumeId, String email) {

        Resume resume = getOwnedResumeOrThrow(resumeId, email);

        resume.setDeleted(true);
        resumeRepository.save(resume);
    }

    public ResumeUploadResponse replaceResume(Long resumeId, MultipartFile file, String email)
            throws IOException {

        Resume resume = getOwnedResumeOrThrow(resumeId, email);
        String originalFileName = validateAndSanitizeFile(file);

        // best-effort cleanup of the old physical file; DB record is the source of truth either way
        try {
            Files.deleteIfExists(Paths.get(resume.getFilePath()));
        } catch (IOException ignored) {
            // old file missing/locked shouldn't block the replace operation
        }

        persistFileAndPopulateResume(resume, file, originalFileName);
        resumeRepository.save(resume);

        return new ResumeUploadResponse(
                "Resume replaced successfully.",
                originalFileName
        );
    }

    // ---- internal helpers ----

    private User getUserOrThrow(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            throw new RuntimeException("User not found.");
        }

        return optionalUser.get();
    }

    private Resume getOwnedResumeOrThrow(Long resumeId, String email) {

        User user = getUserOrThrow(email);

        Resume resume = resumeRepository.findByIdAndDeletedFalse(resumeId)
                .orElseThrow(() -> new ResumeNotFoundException(
                        "Resume not found with id: " + resumeId));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedResumeAccessException(
                    "You do not have access to this resume.");
        }

        return resume;
    }

    /**
     * Validates the uploaded file (non-empty, PDF extension + content type, size limit,
     * safe file name) and returns a sanitized original file name safe for storage/display.
     */
    private String validateAndSanitizeFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Please select a file.");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new InvalidFileException("File exceeds the maximum allowed size of 5MB.");
        }

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.isBlank()) {
            throw new InvalidFileException("Uploaded file must have a name.");
        }

        // strip any path elements a malicious client might smuggle in (path traversal defense)
        String sanitized = Paths.get(originalFileName).getFileName().toString();

        if (!sanitized.toLowerCase().endsWith(".pdf")) {
            throw new InvalidFileException("Only PDF files are allowed.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equalsIgnoreCase("application/pdf")) {
            throw new InvalidFileException("Only PDF files are allowed.");
        }

        return sanitized;
    }

    private void persistFileAndPopulateResume(Resume resume, MultipartFile file, String originalFileName)
            throws IOException {

        Path uploadPath = Paths.get(UPLOAD_DIR);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String storedFileName = UUID.randomUUID() + "_" + originalFileName;
        Path filePath = uploadPath.resolve(storedFileName);

        Files.copy(
                file.getInputStream(),
                filePath,
                StandardCopyOption.REPLACE_EXISTING
        );

        resume.setOriginalFileName(originalFileName);
        resume.setStoredFileName(storedFileName);
        resume.setFilePath(filePath.toString());
        resume.setFileSizeBytes(file.getSize());
        resume.setUploadedAt(LocalDateTime.now());
        resume.setDeleted(false);
    }
}
