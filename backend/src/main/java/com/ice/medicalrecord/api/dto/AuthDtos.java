package com.ice.medicalrecord.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {
    }

    public record PasswordResetRequest(@Email @NotBlank String email) {
    }

    public record PasswordResetResponse(String message, String resetToken) {
    }

    public record PasswordResetConfirmRequest(@NotBlank String token, @Size(min = 8) String newPassword) {
    }
}
