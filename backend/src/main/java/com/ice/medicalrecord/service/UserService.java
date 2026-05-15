package com.ice.medicalrecord.service;

import com.ice.medicalrecord.api.dto.UserDtos.CreateUserRequest;
import com.ice.medicalrecord.api.dto.UserDtos.UpdateUserRequest;
import com.ice.medicalrecord.api.dto.UserDtos.UserResponse;
import com.ice.medicalrecord.domain.DoctorProfile;
import com.ice.medicalrecord.domain.Role;
import com.ice.medicalrecord.domain.User;
import com.ice.medicalrecord.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream().map(Mapper::user).toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listDoctors() {
        return userRepository.findByRoleOrderByNameAsc(Role.DOCTOR).stream().map(Mapper::user).toList();
    }

    @Transactional
    public UserResponse create(CreateUserRequest request, String actorEmail) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalArgumentException("邮箱已存在");
        }
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        if (request.role() == Role.DOCTOR) {
            DoctorProfile profile = new DoctorProfile();
            profile.setUser(user);
            profile.setTitle(defaultText(request.title(), "主治医师"));
            profile.setDepartment(defaultText(request.department(), "全科门诊"));
            profile.setAvatarUrl(request.avatarUrl());
            user.setDoctorProfile(profile);
        }
        User saved = userRepository.save(user);
        auditService.log(actorEmail, "CREATE_USER", "User", saved.getId());
        return Mapper.user(saved);
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request, String actorEmail) {
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("账号不存在"));
        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name());
        }
        if (request.active() != null) {
            user.setActive(request.active());
        }
        if (user.getRole() == Role.DOCTOR) {
            DoctorProfile profile = user.getDoctorProfile();
            if (profile == null) {
                profile = new DoctorProfile();
                profile.setUser(user);
                user.setDoctorProfile(profile);
            }
            if (request.title() != null) {
                profile.setTitle(request.title());
            }
            if (request.department() != null) {
                profile.setDepartment(request.department());
            }
            if (request.avatarUrl() != null) {
                profile.setAvatarUrl(request.avatarUrl());
            }
        }
        auditService.log(actorEmail, "UPDATE_USER", "User", user.getId());
        return Mapper.user(user);
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
