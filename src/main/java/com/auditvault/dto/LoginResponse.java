package com.auditvault.dto;

public class LoginResponse {

    private Long id;
    private String userId;
    private String name;
    private String email;
    private String role;
    private String token;

    public LoginResponse(Long id, String userId, String name, String email, String role, String token) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }
}

