package com.ice.medicalrecord.repository;

import com.ice.medicalrecord.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
