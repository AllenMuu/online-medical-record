package com.ice.medicalrecord.repository;

import com.ice.medicalrecord.domain.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    boolean existsByNameIgnoreCaseAndTeamIgnoreCase(String name, String team);

    boolean existsByNameIgnoreCaseAndTeamIgnoreCaseAndIdNot(String name, String team, Long id);

    Page<Patient> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Patient> findByTeamContainingIgnoreCase(String team, Pageable pageable);

    Page<Patient> findByNameContainingIgnoreCaseAndTeamContainingIgnoreCase(
            String name, String team, Pageable pageable);
}
