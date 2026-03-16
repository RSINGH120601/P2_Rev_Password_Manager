package com.rev.passwordmanager.dto;

public class ProfileResponse {
    private String name;
    private String email;
    private String phone;
    private String username;
    private boolean twoFactorEnabled;

    public ProfileResponse(String name, String email, String phone, String username, boolean twoFactorEnabled) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.username = username;
        this.twoFactorEnabled = twoFactorEnabled;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public boolean isTwoFactorEnabled() { return twoFactorEnabled; }
    public void setTwoFactorEnabled(boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }
}
