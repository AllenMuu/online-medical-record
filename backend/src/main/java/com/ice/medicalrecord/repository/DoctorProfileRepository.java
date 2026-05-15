package com.ice.medicalrecord.repository;

import com.ice.medicalrecord.domain.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {
}
