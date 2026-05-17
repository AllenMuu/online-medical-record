package com.ice.medicalrecord.api;

import com.ice.medicalrecord.api.dto.AuthDtos.LoginRequest;
import com.ice.medicalrecord.api.dto.AuthDtos.ChangePasswordRequest;
import com.ice.medicalrecord.api.dto.AuthDtos.PasswordResetConfirmRequest;
import com.ice.medicalrecord.api.dto.AuthDtos.PasswordResetRequest;
import com.ice.medicalrecord.api.dto.AuthDtos.PasswordResetResponse;
import com.ice.medicalrecord.api.dto.UserDtos.UpdateAvatarResponse;
import com.ice.medicalrecord.api.dto.UserDtos.UserResponse;
import com.ice.medicalrecord.repository.UserRepository;
import com.ice.medicalrecord.service.AuthService;
import com.ice.medicalrecord.service.Mapper;
import com.ice.medicalrecord.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Map;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 认证与账户自助接口。
 * 负责登录、会话查询与密码重置流程。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final UserService userService;

    public AuthController(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            AuthService authService,
            UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.authService = authService;
        this.userService = userService;
    }

    /**
     * 使用邮箱和密码登录，并建立服务端会话。
     */
    @PostMapping("/login")
    public UserResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        httpRequest.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context);
        return me(authentication.getName());
    }

    /**
     * 注销当前会话并清理安全上下文。
     */
    @PostMapping("/logout")
    public Map<String, String> logout(HttpServletRequest request) {
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        SecurityContextHolder.clearContext();
        return Map.of("message", "已退出登录");
    }

    /**
     * 返回当前登录用户的基础信息。
     */
    @GetMapping("/me")
    public UserResponse me(Principal principal) {
        return me(principal.getName());
    }

    /**
     * 为指定邮箱生成密码重置令牌。
     * 当前本地原型环境会直接在响应体中返回令牌。
     */
    @PostMapping("/password-reset/request")
    public PasswordResetResponse requestReset(@Valid @RequestBody PasswordResetRequest request) {
        return authService.createResetToken(request.email());
    }

    /**
     * 使用重置令牌完成密码更新。
     */
    @PostMapping("/password-reset/confirm")
    public Map<String, String> confirmReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.resetPassword(request.token(), request.newPassword());
        return Map.of("message", "密码已重置");
    }

    /**
     * 当前登录用户修改自己的密码。
     */
    @PostMapping("/change-password")
    public Map<String, String> changePassword(
            HttpServletRequest httpRequest,
            Principal principal,
            @Valid @RequestBody ChangePasswordRequest payload) {
        authService.changePassword(principal.getName(), payload.currentPassword(), payload.newPassword());
        if (httpRequest.getSession(false) != null) {
            httpRequest.getSession(false).invalidate();
        }
        SecurityContextHolder.clearContext();
        return Map.of("message", "密码修改成功，请重新登录");
    }

    @PatchMapping("/me/avatar")
    public UpdateAvatarResponse updateMyAvatar(
            Principal principal,
            @RequestPart("avatar") MultipartFile avatarFile) {
        return userService.updateMyAvatar(principal.getName(), avatarFile);
    }

    private UserResponse me(String email) {
        return userRepository.findByEmailIgnoreCase(email).map(Mapper::user)
                .orElseThrow(() -> new IllegalArgumentException("账号不存在"));
    }
}
