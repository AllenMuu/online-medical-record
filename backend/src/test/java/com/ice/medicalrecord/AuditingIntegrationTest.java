package com.ice.medicalrecord;

import static org.assertj.core.api.Assertions.assertThat;

import com.ice.medicalrecord.domain.AuditLog;
import com.ice.medicalrecord.domain.DoctorProfile;
import com.ice.medicalrecord.domain.Gender;
import com.ice.medicalrecord.domain.MedicalRecord;
import com.ice.medicalrecord.domain.Medication;
import com.ice.medicalrecord.domain.PasswordResetToken;
import com.ice.medicalrecord.domain.Patient;
import com.ice.medicalrecord.domain.RecordStatus;
import com.ice.medicalrecord.domain.Role;
import com.ice.medicalrecord.domain.User;
import com.ice.medicalrecord.repository.AuditLogRepository;
import com.ice.medicalrecord.repository.MedicalRecordRepository;
import com.ice.medicalrecord.repository.PasswordResetTokenRepository;
import com.ice.medicalrecord.repository.PatientRepository;
import com.ice.medicalrecord.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class AuditingIntegrationTest {
    @Autowired
    UserRepository userRepository;

    @Autowired
    PatientRepository patientRepository;

    @Autowired
    MedicalRecordRepository medicalRecordRepository;

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    AuditLogRepository auditLogRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void auditFieldsAreFilledOnCreateAndUpdate() throws Exception {
        User systemUser = new User();
        systemUser.setName("系统审计账号");
        systemUser.setEmail("system.audit@example.com");
        systemUser.setPasswordHash(passwordEncoder.encode("Admin123!"));
        systemUser.setRole(Role.ADMIN);
        systemUser = userRepository.saveAndFlush(systemUser);
        assertAuditFields(systemUser, "system");

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(systemUser);
        resetToken.setToken("token-1");
        resetToken.setExpiresAt(java.time.Instant.now().plusSeconds(3600));
        resetToken.setUsed(false);
        resetToken = passwordResetTokenRepository.saveAndFlush(resetToken);
        assertAuditFields(resetToken, "system");

        String creatorAuditor = "doctor.audit@example.com";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(creatorAuditor, "n/a", List.of()));

        User doctor = new User();
        doctor.setName("审计医生");
        doctor.setEmail("doctor.audit@example.com");
        doctor.setPasswordHash(passwordEncoder.encode("Doctor123!"));
        doctor.setRole(Role.DOCTOR);
        DoctorProfile profile = new DoctorProfile();
        profile.setUser(doctor);
        profile.setDepartment("心内科");
        profile.setAvatarUrl("https://example.com/avatar.png");
        doctor.setDoctorProfile(profile);
        doctor = userRepository.saveAndFlush(doctor);
        assertAuditFields(doctor, creatorAuditor);
        assertAuditFields(profile, creatorAuditor);

        Patient patient = new Patient();
        patient.setName("审计患者");
        patient.setGender(Gender.FEMALE);
        patient.setAge(36);
        patient.setTeam("测试团队");
        patient.setPhone("13800000001");
        patient.setBirthDate(LocalDate.of(1990, 1, 1));
        patient.setSummary("初始摘要");
        patient = patientRepository.saveAndFlush(patient);
        assertAuditFields(patient, creatorAuditor);
        var createdAt = patient.getCreatedAt();
        String updaterAuditor = "editor.audit@example.com";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(updaterAuditor, "n/a", List.of()));
        patient.setSummary("更新摘要");
        patient = patientRepository.saveAndFlush(patient);
        assertThat(patient.getCreatedBy()).isEqualTo(creatorAuditor);
        assertThat(patient.getUpdatedAt()).isAfterOrEqualTo(createdAt);
        assertThat(patient.getUpdatedBy()).isEqualTo(updaterAuditor);

        MedicalRecord record = new MedicalRecord();
        record.setPatient(patient);
        record.setDoctor(doctor);
        record.setVisitDate(LocalDate.of(2024, 1, 2));
        record.setVisitTime(LocalTime.of(10, 15));
        record.setDiagnosis("审计诊断");
        record.setComplaint("主诉");
        record.setExamination("查体");
        record.setTreatment("处置");
        record.setPrognosis("预后");
        record.setNotes("备注");
        record.setStatus(RecordStatus.COMPLETED);
        Medication medication = new Medication();
        medication.setMedicalRecord(record);
        medication.setName("阿莫西林");
        medication.setDosage("0.5g tid");
        record.getMedications().add(medication);
        record = medicalRecordRepository.saveAndFlush(record);
        assertAuditFields(record, updaterAuditor);
        assertAuditFields(medication, updaterAuditor);

        AuditLog auditLog = new AuditLog();
        auditLog.setActorEmail(updaterAuditor);
        auditLog.setAction("TEST_AUDIT");
        auditLog.setEntityType("Patient");
        auditLog.setEntityId(patient.getId());
        auditLog = auditLogRepository.saveAndFlush(auditLog);
        assertAuditFields(auditLog, updaterAuditor);
    }

    private void assertAuditFields(com.ice.medicalrecord.domain.AuditableEntity entity, String auditor) {
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
        assertThat(entity.getCreatedBy()).isEqualTo(auditor);
        assertThat(entity.getUpdatedBy()).isEqualTo(auditor);
    }
}
