package com.ritnesh.careerpilot.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ResendOtpRequest {

    @Email(message = "A valid email is required.")
    @NotBlank(message = "Email is required.")
    private String email;

    public ResendOtpRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
