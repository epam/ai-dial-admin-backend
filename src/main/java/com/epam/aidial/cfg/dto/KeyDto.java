package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.utils.SecretUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class KeyDto {

    @NotBlank(message = "Name is required")
    private String name;
    @NotBlank(message = "Key is required")
    private String key;
    private String project;
    private boolean secured;
    private List<String> roles;
    private String description;
    private String projectContactPoint;
    private Long createdAt;
    private Long expiresAt;
    private Long keyGeneratedAt;

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
                + ")";
    }
}
