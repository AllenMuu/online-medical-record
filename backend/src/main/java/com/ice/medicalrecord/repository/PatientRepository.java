package com.ice.medicalrecord.repository;

import com.ice.medicalrecord.domain.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Page<Patient> findByNameContainingIgnoreCaseOrTeamContainingIgnoreCase(String name, String team, Pageable pageable);
}
