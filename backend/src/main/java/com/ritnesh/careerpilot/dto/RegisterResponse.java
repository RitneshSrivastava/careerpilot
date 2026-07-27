package com.ritnesh.careerpilot.dto;

public class RegisterResponse {

    private String message;
    private String fullName;
    private String email;

    public RegisterResponse() {
    }

    public RegisterResponse(String message, String fullName, String email) {
        this.message = message;
        this.fullName = fullName;
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
