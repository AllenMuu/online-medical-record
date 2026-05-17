package com.ice.medicalrecord.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 审计日志实体。
 * 记录关键业务操作的执行人、动作、目标对象与时间。
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 执行操作的账号邮箱。 */
    @Column(nullable = false, length = 80)
    private String actorEmail;

    /** 审计动作编码，例如 CREATE_PATIENT。 */
    @Column(nullable = false, length = 80)
    private String action;

    /** 被操作实体类型。 */
    @Column(nullable = false, length = 80)
    private String entityType;

    /** 被操作实体主键。 */
    @Column(nullable = false)
    private Long entityId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getActorEmail() {
        return actorEmail;
    }

    public void setActorEmail(String actorEmail) {
        this.actorEmail = actorEmail;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

}
