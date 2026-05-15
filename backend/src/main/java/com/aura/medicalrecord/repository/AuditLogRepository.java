package com.aura.medicalrecord.repository;

import com.aura.medicalrecord.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
