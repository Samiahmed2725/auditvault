package com.auditvault.repository;

import com.auditvault.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    // Basic Finder: Find most recent logs
    List<AuditLog> findAllByOrderByTimestampDesc();
}
