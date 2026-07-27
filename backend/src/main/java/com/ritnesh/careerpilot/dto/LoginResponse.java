package com.ritnesh.careerpilot.dto;

public class LoginResponse {

    private String message;
    private String fullName;
    private String email;
    private String token;

    public LoginResponse() {
    }

    public LoginResponse(String message,
                         String fullName,
                         String email,
                         String token) {
        this.message = message;
        this.fullName = fullName;
        this.email = email;
        this.token=token;
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
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}