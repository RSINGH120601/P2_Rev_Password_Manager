package com.rev.passwordmanager.dto;

public class PasswordResponse {

    private Long id;
    private String accountName;
    private String website;
    private String username;
    private String password;
    private String category;
    private String notes;
    private Boolean favorite;

    public PasswordResponse(Long id, String accountName, String website,
                            String username, String password,
                            String category, String notes, Boolean favorite) {
        this.id = id;
        this.accountName = accountName;
        this.website = website;
        this.username = username;
        this.password = password;
        this.category = category;
        this.notes = notes;
        this.favorite = favorite;
    }

    public Long getId() { return id; }
    public String getAccountName() { return accountName; }
    public String getWebsite() { return website; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getCategory() { return category; }
    public String getNotes() { return notes; }
    public Boolean getFavorite() { return favorite; }
}