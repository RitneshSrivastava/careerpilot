package com.ritnesh.careerpilot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    // Defense-in-depth: even though controllers now return dedicated response
    // DTOs (never the raw entity), this ensures the password hash can never
    // accidentally leak if a future endpoint returns a User entity directly.
    @JsonIgnore
    private String password;

    @Column(nullable = false)
    private boolean verified = false;

    private String otpCode;

    private java.time.LocalDateTime otpExpiry;

    @OneToMany(mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Resume> resumes = new ArrayList<>();
}