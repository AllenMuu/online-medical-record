package com.ice.medicalrecord.api.dto;

import com.ice.medicalrecord.domain.RecordStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public final class MedicalRecordDtos {
    private MedicalRecordDtos() {
    }

    public record MedicationRequest(@NotBlank String name, @NotBlank String dosage) {
    }

    public record MedicationResponse(Long id, String name, String dosage) {
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
            RecordStatus status,
            @Valid List<MedicationRequest> medications) {
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
            List<MedicationResponse> medications,
            Instant createdAt,
            Instant updatedAt) {
    }
}
