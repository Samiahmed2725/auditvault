package com.auditvault.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userId;

    private String name;

    @Column(unique = true)
    private String email;

    @JsonIgnore
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String panEncrypted;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "auditor_id")
    @JsonIgnore
    private User auditor;

    private LocalDateTime createdAt = LocalDateTime.now();

    public String getEmail() {
        return email;
    }

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    @JsonIgnore
    public String getPassword() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public String getName() {
        return name;
    }

    public enum Role {
        AUDITOR, CLIENT
    }

    public enum Status {
        ACTIVE, INACTIVE
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @JsonProperty("password")
    public void setPassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public User getAuditor() {
        return auditor;
    }

    public void setAuditor(User auditor) {
        this.auditor = auditor;
    }
}


