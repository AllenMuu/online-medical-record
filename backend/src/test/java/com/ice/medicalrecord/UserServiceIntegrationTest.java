package com.ice.medicalrecord;

import static org.assertj.core.api.Assertions.assertThat;

import com.ice.medicalrecord.api.dto.UserDtos.CreateUserRequest;
import com.ice.medicalrecord.api.dto.UserDtos.UpdateUserRequest;
import com.ice.medicalrecord.api.dto.UserDtos.UserResponse;
import com.ice.medicalrecord.domain.Role;
import com.ice.medicalrecord.domain.User;
import com.ice.medicalrecord.repository.UserRepository;
import com.ice.medicalrecord.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class UserServiceIntegrationTest {
    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void createDoctorDefaultsDepartmentToEmptyAndAllowsUpdate() {
        String email = "new.doctor@example.com";
        UserResponse created = userService.create(
                new CreateUserRequest("新医生", email, "Doctor123!", Role.DOCTOR, null, null),
                "admin@example.com");

        assertThat(created.id()).isNotNull();
        assertThat(created.department()).isEqualTo("");

        User stored = userRepository.findById(created.id()).orElseThrow();
        assertThat(stored.getDoctorProfile()).isNotNull();
        assertThat(stored.getDoctorProfile().getDepartment()).isEqualTo("");

        UserResponse updated = userService.update(created.id(), new UpdateUserRequest(null, null, "心内科", null), "admin@example.com");
        assertThat(updated.department()).isEqualTo("心内科");
        assertThat(userRepository.findById(created.id()).orElseThrow().getDoctorProfile().getDepartment()).isEqualTo("心内科");
    }
}
