package com.auditvault.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String userEmail;

    @Enumerated(EnumType.STRING)
    private User.Role role;

    @Enumerated(EnumType.STRING)
    private Action action;

    private String details; // e.g., "filename: report.pdf, client: 5"

    private LocalDateTime timestamp = LocalDateTime.now();

    public AuditLog() {
    }

    public AuditLog(Long userId, String userEmail, User.Role role, Action action, String details) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.role = role;
        this.action = action;
        this.details = details;
    }

    public enum Action {
        LOGIN, UPLOAD, DOWNLOAD, DELETE
    }

    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getUserEmail() { return userEmail; }
    public User.Role getRole() { return role; }
    public Action getAction() { return action; }
    public String getDetails() { return details; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
