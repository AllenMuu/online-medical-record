package com.ice.medicalrecord.api.dto;

import com.ice.medicalrecord.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class UserDtos {
    private UserDtos() {
    }

    public record UserResponse(
            Long id,
            String name,
            String email,
            Role role,
            boolean active,
            String department,
            String avatarUrl,
            Instant createdAt,
            Instant updatedAt) {
    }

    public record CreateUserRequest(
            @NotBlank String name,
            @Email @NotBlank String email,
            @Size(min = 8) String password,
            @NotNull Role role,
            String department,
            String avatarUrl) {
    }

    public record UpdateUserRequest(
            String name,
            Boolean active,
            String department,
            String avatarUrl) {
    }

    public record UpdateAvatarResponse(String avatarUrl) {
    }
}
