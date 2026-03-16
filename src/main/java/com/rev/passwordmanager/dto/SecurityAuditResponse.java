package com.rev.passwordmanager.dto;

import java.util.List;

public class SecurityAuditResponse {
    private List<PasswordResponse> weakPasswords;
    private List<PasswordResponse> reusedPasswords;
    private List<PasswordResponse> oldPasswords;
    private int securityScore;

    public SecurityAuditResponse() {}

    public SecurityAuditResponse(List<PasswordResponse> weakPasswords, List<PasswordResponse> reusedPasswords, List<PasswordResponse> oldPasswords, int securityScore) {
        this.weakPasswords = weakPasswords;
        this.reusedPasswords = reusedPasswords;
        this.oldPasswords = oldPasswords;
        this.securityScore = securityScore;
    }

    public List<PasswordResponse> getWeakPasswords() {
        return weakPasswords;
    }

    public void setWeakPasswords(List<PasswordResponse> weakPasswords) {
        this.weakPasswords = weakPasswords;
    }

    public List<PasswordResponse> getReusedPasswords() {
        return reusedPasswords;
    }

    public void setReusedPasswords(List<PasswordResponse> reusedPasswords) {
        this.reusedPasswords = reusedPasswords;
    }

    public List<PasswordResponse> getOldPasswords() {
        return oldPasswords;
    }

    public void setOldPasswords(List<PasswordResponse> oldPasswords) {
        this.oldPasswords = oldPasswords;
    }

    public int getSecurityScore() {
        return securityScore;
    }

    public void setSecurityScore(int securityScore) {
        this.securityScore = securityScore;
    }
}
