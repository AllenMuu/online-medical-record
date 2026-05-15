package com.ice.medicalrecord.api.dto;

import com.ice.medicalrecord.domain.Gender;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public final class PatientDtos {
    private PatientDtos() {
    }

    public record PatientResponse(
            Long id,
            String name,
            Gender gender,
            Integer age,
            String team,
            String phone,
            LocalDate birthDate,
            String summary) {
    }

    public record UpsertPatientRequest(
            @NotBlank String name,
            @NotNull Gender gender,
            @NotNull @Min(0) @Max(130) Integer age,
            @NotBlank String team,
            String phone,
            LocalDate birthDate,
            String summary) {
    }
}
