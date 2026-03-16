package com.rev.passwordmanager.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20,
            message = "Username must be between 3 and 20 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Master password is required")
    @Size(min = 6,
            message = "Password must be at least 6 characters")
    private String masterPassword;

    @NotNull(message = "Security answers are required")
    @Size(min = 3,
            message = "Minimum 3 security questions must be answered")
    private List<SecurityAnswerDTO> securityAnswers;

    // ===== GETTERS & SETTERS =====

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMasterPassword() {
        return masterPassword;
    }

    public void setMasterPassword(String masterPassword) {
        this.masterPassword = masterPassword;
    }

    public List<SecurityAnswerDTO> getSecurityAnswers() {
        return securityAnswers;
    }

    public void setSecurityAnswers(List<SecurityAnswerDTO> securityAnswers) {
        this.securityAnswers = securityAnswers;
    }
}