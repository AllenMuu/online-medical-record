package com.aura.medicalrecord.repository;

import com.aura.medicalrecord.domain.MedicalRecord;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    @Query("""
            select r from MedicalRecord r
            join r.patient p
            join r.doctor d
            where (:query is null or lower(p.name) like lower(concat('%', :query, '%')) or lower(r.diagnosis) like lower(concat('%', :query, '%')))
              and (:doctorId is null or d.id = :doctorId)
              and (:fromDate is null or r.visitDate >= :fromDate)
              and (:toDate is null or r.visitDate <= :toDate)
            order by r.visitDate desc, r.visitTime desc
            """)
    Page<MedicalRecord> search(
            @Param("query") String query,
            @Param("doctorId") Long doctorId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable);

    long countByVisitDateBetween(LocalDate fromDate, LocalDate toDate);
}
