package com.ice.medicalrecord.service;

import com.ice.medicalrecord.api.dto.MedicalRecordDtos.MedicalRecordResponse;
import com.ice.medicalrecord.api.dto.MedicalRecordDtos.UpsertMedicalRecordRequest;
import com.ice.medicalrecord.domain.MedicalRecord;
import com.ice.medicalrecord.domain.Medication;
import com.ice.medicalrecord.domain.Patient;
import com.ice.medicalrecord.domain.RecordStatus;
import com.ice.medicalrecord.domain.User;
import com.ice.medicalrecord.repository.MedicalRecordRepository;
import com.ice.medicalrecord.repository.PatientRepository;
import com.ice.medicalrecord.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MedicalRecordService {
    private static final Sort DEFAULT_SEARCH_SORT =
            Sort.by(Sort.Order.desc("visitDate"), Sort.Order.desc("visitTime"));

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
        Pageable sortedPageable = pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), DEFAULT_SEARCH_SORT);

        Specification<MedicalRecord> specification = (root, ignoredQuery, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(normalizedQuery)) {
                String pattern = "%" + normalizedQuery.toLowerCase(Locale.ROOT) + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("patient").get("name")), pattern),
                        builder.like(builder.lower(root.get("diagnosis")), pattern)));
            }
            if (doctorId != null) {
                predicates.add(builder.equal(root.get("doctor").get("id"), doctorId));
            }
            if (fromDate != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("visitDate"), fromDate));
            }
            if (toDate != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("visitDate"), toDate));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };

        return recordRepository.findAll(specification, sortedPageable).map(Mapper::medicalRecord);
    }

    @Transactional(readOnly = true)
    public MedicalRecordResponse get(Long id) {
        return recordRepository.findById(id).map(Mapper::medicalRecord)
                .orElseThrow(() -> new EntityNotFoundException("病历不存在"));
    }

    @Transactional
    public MedicalRecordResponse create(UpsertMedicalRecordRequest request, String actorEmail) {
        MedicalRecord record = new MedicalRecord();
        applyRequest(record, request);
        MedicalRecord saved = recordRepository.save(record);
        auditService.log(actorEmail, "CREATE_MEDICAL_RECORD", "MedicalRecord", saved.getId());
        return Mapper.medicalRecord(saved);
    }

    @Transactional
    public MedicalRecordResponse update(Long id, UpsertMedicalRecordRequest request, String actorEmail) {
        MedicalRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("病历不存在"));
        applyRequest(record, request);
        MedicalRecord saved = recordRepository.save(record);
        auditService.log(actorEmail, "UPDATE_MEDICAL_RECORD", "MedicalRecord", saved.getId());
        return Mapper.medicalRecord(saved);
    }

    private void applyRequest(MedicalRecord record, UpsertMedicalRecordRequest request) {
        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new EntityNotFoundException("患者不存在"));
        User doctor = userRepository.findById(request.doctorId())
                .orElseThrow(() -> new EntityNotFoundException("医生不存在"));

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

        record.getMedications().clear();
        if (request.medications() == null) {
            return;
        }

        request.medications().forEach(item -> {
            Medication medication = new Medication();
            medication.setMedicalRecord(record);
            medication.setName(item.name());
            medication.setDosage(item.dosage());
            record.getMedications().add(medication);
        });
    }
}
