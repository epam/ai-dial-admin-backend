package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.utils.SecretUtils;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.List;
import java.util.TreeSet;

@Data
public class KeyDto {

    @NotBlank(message = "Name is required")
    private String name;
    @NotBlank(message = "Key is required")
    private String key;
    @NotBlank(message = "DisplayName is required")
    private String displayName;
    @NotBlank(message = "Project is required")
    private String project;
    private boolean secured;
    private List<String> roles;
    private String description;
    private String projectContactPoint;
    @EqualsAndHashCode.Exclude
    private Instant createdAt;
    @EqualsAndHashCode.Exclude
    private Instant updatedAt;
    private Instant expiresAt;
    private Instant keyGeneratedAt;
    private ValidityStateDto validityState;
    private TreeSet<String> topics;
    private List<String> allowedIpAddressRanges;

    public String toString() {
        return "KeyDto(name=" + this.getName()
                + ", key=" + SecretUtils.mask(this.getKey())
                + ", project=" + this.getProject()
                + ", secured=" + this.isSecured()
                + ", roles=" + this.getRoles()
                + ", description=" + this.getDescription()
                + ", projectContactPoint=" + this.getProjectContactPoint()
                + ", createdAt=" + this.getCreatedAt()
                + ", expiresAt=" + this.getExpiresAt()
                + ", keyGeneratedAt=" + this.getKeyGeneratedAt()
                + ", validityState=" + this.getValidityState()
                + ")";
    }
}