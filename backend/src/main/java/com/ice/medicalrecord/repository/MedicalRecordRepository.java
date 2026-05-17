package com.ice.medicalrecord.repository;

import com.ice.medicalrecord.domain.MedicalRecord;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long>, JpaSpecificationExecutor<MedicalRecord> {
    long countByVisitDateBetween(LocalDate fromDate, LocalDate toDate);

    boolean existsByPatientId(Long patientId);

    boolean existsByDoctorId(Long doctorId);
}
