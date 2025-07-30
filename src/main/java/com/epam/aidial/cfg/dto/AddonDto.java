package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.Instant;
import java.util.List;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AddonDto extends RoleBasedDto {
    @NotBlank(message = "Name is required")
    private String name;

    private String endpoint;
    private String iconUrl;
    private String description;
    private String displayName;
    private List<String> inputAttachmentTypes;
    private Integer maxInputAttachments;
    private Boolean forwardAuthToken;
    private String author;
    @EqualsAndHashCode.Exclude
    private Instant createdAt;
    @EqualsAndHashCode.Exclude
    private Instant updatedAt;
    private List<String> dependencies;
}
