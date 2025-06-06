package com.epam.aidial.cfg.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class AuditActivityDto {
    private UUID activityId;
    private String activityType;
    private String resourceType;
    private String resourceId;
    private Long epochTimestampMs;
    private String initiatedAuthor;
    private String initiatedEmail;
    private Integer revision;
}