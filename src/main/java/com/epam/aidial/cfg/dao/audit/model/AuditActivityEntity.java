package com.epam.aidial.cfg.dao.audit.model;

import com.epam.aidial.cfg.domain.model.activity.ActivityResourceType;
import com.epam.aidial.cfg.domain.model.activity.ActivityType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Data
@Entity
@Table
public class AuditActivityEntity {
    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID activityId;
    @Enumerated(EnumType.STRING)
    private ActivityType activityType;
    @Enumerated(EnumType.STRING)
    private ActivityResourceType resourceType;
    private String resourceId;
    private Long epochTimestampMs;
    private String initiatedAuthor;
    private String initiatedEmail;
    private Integer revision;
}