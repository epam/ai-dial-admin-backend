package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AssistantDto extends RoleBasedDto {

    @NotBlank(message = "Name is required")
    private String name;
    private String iconUrl;
    private String description;
    @NotBlank(message = "DisplayName is required")
    private String displayName;
    private List<String> inputAttachmentTypes;
    private Integer maxInputAttachments;
    private Boolean forwardAuthToken;
    private List<String> topics;
    private Map<String, String> defaults;
    private String author;
    @EqualsAndHashCode.Exclude
    private Instant createdAt;
    @EqualsAndHashCode.Exclude
    private Instant updatedAt;
    private List<String> dependencies;
}
