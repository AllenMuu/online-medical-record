package com.ice.medicalrecord.api.dto;

import com.ice.medicalrecord.domain.RecordStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public final class MedicalRecordDtos {
    private MedicalRecordDtos() {
    }

    public record UpsertMedicalRecordRequest(
            @NotNull Long patientId,
            @NotNull Long doctorId,
            @NotNull LocalDate visitDate,
            @NotNull LocalTime visitTime,
            @NotBlank String diagnosis,
            String complaint,
            String examination,
            String treatment,
            String prognosis,
            String notes,
            RecordStatus status) {
    }

    public record MedicalRecordResponse(
            Long id,
            Long patientId,
            String patientName,
            String patientGender,
            Integer patientAge,
            Long doctorId,
            String doctorName,
            LocalDate visitDate,
            LocalTime visitTime,
            String diagnosis,
            String complaint,
            String examination,
            String treatment,
            String prognosis,
            String notes,
            RecordStatus status,
            Instant createdAt,
            Instant updatedAt) {
    }
}
