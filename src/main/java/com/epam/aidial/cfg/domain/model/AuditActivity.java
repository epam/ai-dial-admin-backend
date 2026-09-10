package com.epam.aidial.cfg.domain.model;

import lombok.Data;

import java.util.UUID;

@Data
public class AuditActivity {
    private UUID activityId;
    private String activityType;
    private String resourceType;
    private String resourceId;
    private Long epochTimestampMs;
    private String initiatedAuthor;
    private String initiatedEmail;
    private Integer revision;
    private UUID parentActivityId;
    private String operationMetadata;
}
