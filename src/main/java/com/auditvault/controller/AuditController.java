package com.auditvault.controller;

import com.auditvault.model.AuditLog;
import com.auditvault.model.User;
import com.auditvault.repository.AuditLogRepository;
import com.auditvault.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditController {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditController(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<AuditLog> getAuditLogs() {
        User user = getAuthenticatedUser();
        
        // Strictly Enforce Access: Only Auditors can see logs
        if (user.getRole() != User.Role.CA) {
            throw new org.springframework.security.access.AccessDeniedException("Access Denied: Clients cannot view audit logs.");
        }

        return auditLogRepository.findAllByOrderByTimestampDesc();
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
