package com.auditvault.service;

import com.auditvault.model.AccessLog;
import com.auditvault.repository.AccessLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AccessLogService {

    private final AccessLogRepository accessLogRepository;

    public AccessLogService(AccessLogRepository accessLogRepository) {
        this.accessLogRepository = accessLogRepository;
    }

    public void log(AccessLog log) {
        accessLogRepository.save(log);
    }
}
