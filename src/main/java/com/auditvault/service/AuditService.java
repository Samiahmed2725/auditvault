package com.auditvault.service;

import com.auditvault.model.AuditLog;
import com.auditvault.model.User;
import com.auditvault.repository.AuditLogRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Async
    public void logAction(User user, AuditLog.Action action, String details) {
        try {
            AuditLog log = new AuditLog(
                    user.getId(),
                    user.getEmail(),
                    user.getRole(),
                    action,
                    details
            );
            auditLogRepository.save(log);
            System.out.println("📝 Audit Log Saved: " + user.getEmail() + " performed " + action);
        } catch (Exception e) {
            // Requirement: Logging failures must NOT break main business flow
            System.err.println("❌ Failed to save audit log: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
