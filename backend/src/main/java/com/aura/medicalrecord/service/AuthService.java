package com.aura.medicalrecord.service;

import com.aura.medicalrecord.api.dto.AuthDtos.PasswordResetResponse;
import com.aura.medicalrecord.domain.PasswordResetToken;
import com.aura.medicalrecord.domain.User;
import com.aura.medicalrecord.repository.PasswordResetTokenRepository;
import com.aura.medicalrecord.repository.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public PasswordResetResponse createResetToken(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .map(user -> {
                    PasswordResetToken resetToken = new PasswordResetToken();
                    resetToken.setUser(user);
                    resetToken.setToken(UUID.randomUUID().toString());
                    resetToken.setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES));
                    tokenRepository.save(resetToken);
                    return new PasswordResetResponse("已生成本地重置令牌", resetToken.getToken());
                })
                .orElseGet(() -> new PasswordResetResponse("如果账号存在，将生成重置令牌", null));
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("重置令牌无效"));
        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("重置令牌已过期");
        }
        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        resetToken.setUsed(true);
    }
}
