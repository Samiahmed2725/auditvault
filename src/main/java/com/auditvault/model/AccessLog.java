package com.auditvault.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "access_logs")
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "document_id")
    private Document document;

    @Enumerated(EnumType.STRING)
    private Action action;

    private LocalDateTime timestamp = LocalDateTime.now();

    private String ipAddress;

    // Getters and Setters
}

enum Action {
    UPLOAD, DOWNLOAD
}

