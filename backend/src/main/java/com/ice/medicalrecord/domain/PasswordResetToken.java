package com.ice.medicalrecord.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * 密码重置令牌实体。
 * 用于跟踪重置请求、过期时间和使用状态。
 */
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 令牌所属用户。 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    /** 唯一重置令牌。 */
    @Column(nullable = false, unique = true, length = 120)
    private String token;

    /** 令牌失效时间。 */
    @Column(nullable = false)
    private Instant expiresAt;

    /** 令牌是否已经被使用。 */
    @Column(nullable = false)
    private boolean used;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}
