package com.ice.medicalrecord.api;

import com.ice.medicalrecord.api.dto.AuthDtos.LoginRequest;
import com.ice.medicalrecord.api.dto.AuthDtos.PasswordResetConfirmRequest;
import com.ice.medicalrecord.api.dto.AuthDtos.PasswordResetRequest;
import com.ice.medicalrecord.api.dto.AuthDtos.PasswordResetResponse;
import com.ice.medicalrecord.api.dto.UserDtos.UserResponse;
import com.ice.medicalrecord.repository.UserRepository;
import com.ice.medicalrecord.service.AuthService;
import com.ice.medicalrecord.service.Mapper;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final AuthService authService;

    public AuthController(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.authService = authService;
    }

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

    @PostMapping("/logout")
    public Map<String, String> logout(HttpServletRequest request) {
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        SecurityContextHolder.clearContext();
        return Map.of("message", "已退出登录");
    }

    @GetMapping("/me")
    public UserResponse me(Principal principal) {
        return me(principal.getName());
    }

    @PostMapping("/password-reset/request")
    public PasswordResetResponse requestReset(@Valid @RequestBody PasswordResetRequest request) {
        return authService.createResetToken(request.email());
    }

    @PostMapping("/password-reset/confirm")
    public Map<String, String> confirmReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.resetPassword(request.token(), request.newPassword());
        return Map.of("message", "密码已重置");
    }

    private UserResponse me(String email) {
        return userRepository.findByEmailIgnoreCase(email).map(Mapper::user)
                .orElseThrow(() -> new IllegalArgumentException("账号不存在"));
    }
}
