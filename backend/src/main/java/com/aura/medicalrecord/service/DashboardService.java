package com.aura.medicalrecord.service;

import com.aura.medicalrecord.api.dto.DashboardSummary;
import com.aura.medicalrecord.domain.Role;
import com.aura.medicalrecord.repository.MedicalRecordRepository;
import com.aura.medicalrecord.repository.PatientRepository;
import com.aura.medicalrecord.repository.UserRepository;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private final PatientRepository patientRepository;
    private final MedicalRecordRepository recordRepository;
    private final UserRepository userRepository;

    public DashboardService(
            PatientRepository patientRepository,
            MedicalRecordRepository recordRepository,
            UserRepository userRepository) {
        this.patientRepository = patientRepository;
        this.recordRepository = recordRepository;
        this.userRepository = userRepository;
    }

    public DashboardSummary summary() {
        LocalDate now = LocalDate.now();
        LocalDate start = now.withDayOfMonth(1);
        return new DashboardSummary(
                patientRepository.count(),
                recordRepository.count(),
                recordRepository.countByVisitDateBetween(start, now),
                userRepository.findByRoleOrderByNameAsc(Role.DOCTOR).size());
    }
}
