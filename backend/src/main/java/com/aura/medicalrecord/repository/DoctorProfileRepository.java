package com.aura.medicalrecord.repository;

import com.aura.medicalrecord.domain.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {
}
