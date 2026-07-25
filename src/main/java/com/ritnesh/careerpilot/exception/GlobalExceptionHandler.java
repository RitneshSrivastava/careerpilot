package com.ritnesh.careerpilot.exception;

import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<String> handleDuplicateEmail(DuplicateEmailException ex) {

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);

    }
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<String> handleInvalidCredentials(InvalidCredentialsException ex) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ex.getMessage());
    }

    @ExceptionHandler(ResumeNotFoundException.class)
    public ResponseEntity<String> handleResumeNotFound(ResumeNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedResumeAccessException.class)
    public ResponseEntity<String> handleUnauthorizedResumeAccess(UnauthorizedResumeAccessException ex) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ex.getMessage());
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<String> handleInvalidFile(InvalidFileException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    @ExceptionHandler(AnalysisNotFoundException.class)
    public ResponseEntity<String> handleAnalysisNotFound(AnalysisNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(AnalysisRateLimitException.class)
    public ResponseEntity<String> handleAnalysisRateLimit(AnalysisRateLimitException ex) {

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ex.getMessage());
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<String> handlePropertyReference(PropertyReferenceException ex) {

        // Thrown when a ?sort=fieldName query param doesn't match a real field
        // (e.g. Swagger UI's default placeholder value of "string").
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Invalid sort field: '" + ex.getPropertyName() +
                        "'. Valid fields include: id, originalFileName, uploadedAt.");
    }

}