package com.ice.medicalrecord.service;

import com.ice.medicalrecord.api.dto.UserDtos.CreateUserRequest;
import com.ice.medicalrecord.api.dto.UserDtos.UpdateAvatarResponse;
import com.ice.medicalrecord.api.dto.UserDtos.UpdateUserRequest;
import com.ice.medicalrecord.api.dto.UserDtos.UserResponse;
import com.ice.medicalrecord.domain.DoctorProfile;
import com.ice.medicalrecord.domain.Role;
import com.ice.medicalrecord.domain.User;
import com.ice.medicalrecord.repository.MedicalRecordRepository;
import com.ice.medicalrecord.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService {
    private static final long MAX_AVATAR_SIZE_BYTES = 2 * 1024 * 1024;

    private final UserRepository userRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final Path avatarStoragePath;

    public UserService(
            UserRepository userRepository,
            MedicalRecordRepository medicalRecordRepository,
            PasswordEncoder passwordEncoder,
            AuditService auditService,
            @Value("${app.storage.avatar-dir:backend/uploads/avatars}") String avatarDir) {
        this.userRepository = userRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.avatarStoragePath = Path.of(avatarDir).toAbsolutePath().normalize();
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> listUsers(Role role, String nameQuery, Pageable pageable) {
        Specification<User> specification = (root, query, builder) -> builder.conjunction();
        if (role != null) {
            specification = specification.and((root, query, builder) -> builder.equal(root.get("role"), role));
        }

        String normalizedName = normalize(nameQuery);
        if (normalizedName != null) {
            String keyword = "%" + normalizedName.toLowerCase(Locale.ROOT) + "%";
            specification = specification.and(
                    (root, query, builder) -> builder.like(builder.lower(root.get("name")), keyword));
        }

        return userRepository.findAll(specification, sortByName(pageable)).map(Mapper::user);
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
        user.setAvatarUrl(request.avatarUrl());
        if (request.role() == Role.DOCTOR) {
            DoctorProfile profile = new DoctorProfile();
            profile.setUser(user);
            profile.setDepartment(defaultText(request.department(), ""));
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
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }
        if (user.getRole() == Role.DOCTOR) {
            DoctorProfile profile = user.getDoctorProfile();
            if (profile == null) {
                profile = new DoctorProfile();
                profile.setUser(user);
                user.setDoctorProfile(profile);
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

    @Transactional
    public void delete(Long id, String actorEmail) {
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("账号不存在"));
        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("管理员账号不支持删除");
        }
        if (medicalRecordRepository.existsByDoctorId(id)) {
            throw new IllegalArgumentException("该医生已有病历记录，无法删除");
        }
        userRepository.delete(user);
        auditService.log(actorEmail, "DELETE_USER", "User", id);
    }

    @Transactional
    public UpdateAvatarResponse updateMyAvatar(String email, MultipartFile avatarFile) {
        if (avatarFile == null || avatarFile.isEmpty()) {
            throw new IllegalArgumentException("请选择头像图片");
        }
        if (avatarFile.getSize() > MAX_AVATAR_SIZE_BYTES) {
            throw new IllegalArgumentException("头像图片不能超过 2MB");
        }

        String contentType = avatarFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("头像仅支持图片文件");
        }

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new EntityNotFoundException("账号不存在"));

        String extension = resolveImageExtension(contentType, avatarFile.getOriginalFilename());
        String storedFileName = user.getId() + "-" + UUID.randomUUID() + extension;
        try {
            Files.createDirectories(avatarStoragePath);
            Path targetPath = avatarStoragePath.resolve(storedFileName);
            try (InputStream inputStream = avatarFile.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("头像保存失败，请稍后重试");
        }

        String avatarUrl = "/uploads/avatars/" + storedFileName;
        user.setAvatarUrl(avatarUrl);
        DoctorProfile profile = user.getDoctorProfile();
        if (profile != null) {
            profile.setAvatarUrl(avatarUrl);
        }
        auditService.log(email, "UPDATE_MY_AVATAR", "User", user.getId());
        return new UpdateAvatarResponse(avatarUrl);
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private Pageable sortByName(Pageable pageable) {
        if (pageable.getSort().isSorted()) {
            return pageable;
        }
        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Order.asc("name"), Sort.Order.asc("id")));
    }

    private String resolveImageExtension(String contentType, String originalFilename) {
        if (MediaType.IMAGE_JPEG_VALUE.equals(contentType)) {
            return ".jpg";
        }
        if (MediaType.IMAGE_PNG_VALUE.equals(contentType)) {
            return ".png";
        }
        if ("image/webp".equals(contentType)) {
            return ".webp";
        }

        String fileExtension = StringUtils.getFilenameExtension(originalFilename);
        if (fileExtension == null || fileExtension.isBlank()) {
            throw new IllegalArgumentException("无法识别头像图片格式");
        }
        return "." + fileExtension.toLowerCase(Locale.ROOT);
    }
}
