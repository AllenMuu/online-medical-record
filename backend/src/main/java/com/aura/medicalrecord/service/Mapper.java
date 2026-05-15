package com.aura.medicalrecord.service;

import com.aura.medicalrecord.api.dto.MedicalRecordDtos.MedicalRecordResponse;
import com.aura.medicalrecord.api.dto.MedicalRecordDtos.MedicationResponse;
import com.aura.medicalrecord.api.dto.PatientDtos.PatientResponse;
import com.aura.medicalrecord.api.dto.UserDtos.UserResponse;
import com.aura.medicalrecord.domain.DoctorProfile;
import com.aura.medicalrecord.domain.MedicalRecord;
import com.aura.medicalrecord.domain.Patient;
import com.aura.medicalrecord.domain.User;

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
                profile == null ? null : profile.getAvatarUrl());
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
                patient.getSummary());
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
                        .toList());
    }
}
