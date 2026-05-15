package com.ice.medicalrecord.service;

import com.ice.medicalrecord.api.dto.PatientDtos.PatientResponse;
import com.ice.medicalrecord.api.dto.PatientDtos.UpsertPatientRequest;
import com.ice.medicalrecord.domain.Patient;
import com.ice.medicalrecord.repository.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final AuditService auditService;

    public PatientService(PatientRepository patientRepository, AuditService auditService) {
        this.patientRepository = patientRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Page<PatientResponse> list(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return patientRepository.findAll(pageable).map(Mapper::patient);
        }
        return patientRepository.findByNameContainingIgnoreCaseOrTeamContainingIgnoreCase(query, query, pageable)
                .map(Mapper::patient);
    }

    @Transactional
    public PatientResponse create(UpsertPatientRequest request, String actorEmail) {
        Patient patient = new Patient();
        apply(patient, request);
        Patient saved = patientRepository.save(patient);
        auditService.log(actorEmail, "CREATE_PATIENT", "Patient", saved.getId());
        return Mapper.patient(saved);
    }

    @Transactional
    public PatientResponse update(Long id, UpsertPatientRequest request, String actorEmail) {
        Patient patient = patientRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("患者不存在"));
        apply(patient, request);
        auditService.log(actorEmail, "UPDATE_PATIENT", "Patient", patient.getId());
        return Mapper.patient(patient);
    }

    private void apply(Patient patient, UpsertPatientRequest request) {
        patient.setName(request.name());
        patient.setGender(request.gender());
        patient.setAge(request.age());
        patient.setTeam(request.team());
        patient.setPhone(request.phone());
        patient.setBirthDate(request.birthDate());
        patient.setSummary(request.summary());
    }
}
