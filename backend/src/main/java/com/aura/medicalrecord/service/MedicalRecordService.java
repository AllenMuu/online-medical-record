package com.aura.medicalrecord.service;

import com.aura.medicalrecord.api.dto.MedicalRecordDtos.CreateMedicalRecordRequest;
import com.aura.medicalrecord.api.dto.MedicalRecordDtos.MedicalRecordResponse;
import com.aura.medicalrecord.domain.MedicalRecord;
import com.aura.medicalrecord.domain.Medication;
import com.aura.medicalrecord.domain.Patient;
import com.aura.medicalrecord.domain.RecordStatus;
import com.aura.medicalrecord.domain.User;
import com.aura.medicalrecord.repository.MedicalRecordRepository;
import com.aura.medicalrecord.repository.PatientRepository;
import com.aura.medicalrecord.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MedicalRecordService {
    private final MedicalRecordRepository recordRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public MedicalRecordService(
            MedicalRecordRepository recordRepository,
            PatientRepository patientRepository,
            UserRepository userRepository,
            AuditService auditService) {
        this.recordRepository = recordRepository;
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Page<MedicalRecordResponse> search(String query, Long doctorId, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        String normalizedQuery = query == null || query.isBlank() ? null : query.trim();
        return recordRepository.search(normalizedQuery, doctorId, fromDate, toDate, pageable).map(Mapper::medicalRecord);
    }

    @Transactional(readOnly = true)
    public MedicalRecordResponse get(Long id) {
        return recordRepository.findById(id).map(Mapper::medicalRecord)
                .orElseThrow(() -> new EntityNotFoundException("病历不存在"));
    }

    @Transactional
    public MedicalRecordResponse create(CreateMedicalRecordRequest request, String actorEmail) {
        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new EntityNotFoundException("患者不存在"));
        User doctor = userRepository.findById(request.doctorId())
                .orElseThrow(() -> new EntityNotFoundException("医生不存在"));

        MedicalRecord record = new MedicalRecord();
        record.setPatient(patient);
        record.setDoctor(doctor);
        record.setVisitDate(request.visitDate());
        record.setVisitTime(request.visitTime());
        record.setDiagnosis(request.diagnosis());
        record.setComplaint(request.complaint());
        record.setExamination(request.examination());
        record.setTreatment(request.treatment());
        record.setPrognosis(request.prognosis());
        record.setNotes(request.notes());
        record.setStatus(request.status() == null ? RecordStatus.COMPLETED : request.status());
        if (request.medications() != null) {
            request.medications().forEach(item -> {
                Medication medication = new Medication();
                medication.setMedicalRecord(record);
                medication.setName(item.name());
                medication.setDosage(item.dosage());
                record.getMedications().add(medication);
            });
        }
        MedicalRecord saved = recordRepository.save(record);
        auditService.log(actorEmail, "CREATE_MEDICAL_RECORD", "MedicalRecord", saved.getId());
        return Mapper.medicalRecord(saved);
    }
}
