package com.ice.medicalrecord.service;

import com.ice.medicalrecord.api.dto.PatientDtos.PatientResponse;
import com.ice.medicalrecord.api.dto.PatientDtos.UpsertPatientRequest;
import com.ice.medicalrecord.domain.Patient;
import com.ice.medicalrecord.repository.MedicalRecordRepository;
import com.ice.medicalrecord.repository.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final AuditService auditService;

    public PatientService(
            PatientRepository patientRepository,
            MedicalRecordRepository medicalRecordRepository,
            AuditService auditService) {
        this.patientRepository = patientRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Page<PatientResponse> list(String nameQuery, String teamQuery, Pageable pageable) {
        String normalizedName = normalize(nameQuery);
        String normalizedTeam = normalize(teamQuery);

        if (normalizedName == null && normalizedTeam == null) {
            return patientRepository.findAll(pageable).map(Mapper::patient);
        }
        if (normalizedName == null) {
            return patientRepository.findByTeamContainingIgnoreCase(normalizedTeam, pageable).map(Mapper::patient);
        }
        if (normalizedTeam == null) {
            return patientRepository.findByNameContainingIgnoreCase(normalizedName, pageable).map(Mapper::patient);
        }
        return patientRepository
                .findByNameContainingIgnoreCaseAndTeamContainingIgnoreCase(normalizedName, normalizedTeam, pageable)
                .map(Mapper::patient);
    }

    @Transactional
    public PatientResponse create(UpsertPatientRequest request, String actorEmail) {
        Patient patient = new Patient();
        validateUnique(request, null);
        apply(patient, request);
        Patient saved = patientRepository.save(patient);
        auditService.log(actorEmail, "CREATE_PATIENT", "Patient", saved.getId());
        return Mapper.patient(saved);
    }

    @Transactional
    public PatientResponse update(Long id, UpsertPatientRequest request, String actorEmail) {
        Patient patient = patientRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("患者不存在"));
        validateUnique(request, id);
        apply(patient, request);
        auditService.log(actorEmail, "UPDATE_PATIENT", "Patient", patient.getId());
        return Mapper.patient(patient);
    }

    @Transactional
    public void delete(Long id, String actorEmail) {
        Patient patient = patientRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("患者不存在"));
        if (medicalRecordRepository.existsByPatientId(id)) {
            throw new IllegalArgumentException("该患者已有病历记录，无法删除");
        }
        patientRepository.delete(patient);
        auditService.log(actorEmail, "DELETE_PATIENT", "Patient", id);
    }

    private void apply(Patient patient, UpsertPatientRequest request) {
        patient.setName(request.name().trim());
        patient.setGender(request.gender());
        patient.setAge(request.age());
        patient.setTeam(request.team().trim());
        patient.setPhone(request.phone());
        patient.setBirthDate(request.birthDate());
        patient.setSummary(request.summary());
    }

    private void validateUnique(UpsertPatientRequest request, Long currentId) {
        String name = request.name().trim();
        String team = request.team().trim();
        boolean exists = currentId == null
                ? patientRepository.existsByNameIgnoreCaseAndTeamIgnoreCase(name, team)
                : patientRepository.existsByNameIgnoreCaseAndTeamIgnoreCaseAndIdNot(name, team, currentId);
        if (exists) {
            throw new IllegalArgumentException("同一队伍下患者姓名不能重复");
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
