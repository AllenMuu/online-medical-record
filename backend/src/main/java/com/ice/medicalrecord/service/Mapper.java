package com.ice.medicalrecord.service;

import com.ice.medicalrecord.api.dto.MedicalRecordDtos.MedicalRecordResponse;
import com.ice.medicalrecord.api.dto.MedicalRecordDtos.MedicationResponse;
import com.ice.medicalrecord.api.dto.PatientDtos.PatientResponse;
import com.ice.medicalrecord.api.dto.UserDtos.UserResponse;
import com.ice.medicalrecord.domain.DoctorProfile;
import com.ice.medicalrecord.domain.MedicalRecord;
import com.ice.medicalrecord.domain.Patient;
import com.ice.medicalrecord.domain.User;

public final class Mapper {
    private Mapper() {
    }

    public static UserResponse user(User user) {
        DoctorProfile profile = user.getDoctorProfile();
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                profile == null ? null : profile.getTitle(),
                profile == null ? null : profile.getDepartment(),
                profile == null ? null : profile.getAvatarUrl(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    public static PatientResponse patient(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getName(),
                patient.getGender(),
                patient.getAge(),
                patient.getTeam(),
                patient.getPhone(),
                patient.getBirthDate(),
                patient.getSummary(),
                patient.getCreatedAt(),
                patient.getUpdatedAt());
    }

    public static MedicalRecordResponse medicalRecord(MedicalRecord record) {
        Patient patient = record.getPatient();
        User doctor = record.getDoctor();
        return new MedicalRecordResponse(
                record.getId(),
                patient.getId(),
                patient.getName(),
                patient.getGender().name(),
                patient.getAge(),
                doctor.getId(),
                doctor.getName(),
                record.getVisitDate(),
                record.getVisitTime(),
                record.getDiagnosis(),
                record.getComplaint(),
                record.getExamination(),
                record.getTreatment(),
                record.getPrognosis(),
                record.getNotes(),
                record.getStatus(),
                record.getMedications().stream()
                        .map(med -> new MedicationResponse(med.getId(), med.getName(), med.getDosage()))
                        .toList(),
                record.getCreatedAt(),
                record.getUpdatedAt());
    }
}
