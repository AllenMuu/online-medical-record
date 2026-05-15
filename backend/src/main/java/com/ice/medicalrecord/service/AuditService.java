package com.ice.medicalrecord.service;

import com.ice.medicalrecord.domain.AuditLog;
import com.ice.medicalrecord.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void log(String actorEmail, String action, String entityType, Long entityId) {
        AuditLog log = new AuditLog();
        log.setActorEmail(actorEmail == null ? "system" : actorEmail);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        auditLogRepository.save(log);
    }
}
