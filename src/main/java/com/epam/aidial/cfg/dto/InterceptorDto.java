package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.dto.source.InterceptorSourceDto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.List;

@Data
public class InterceptorDto {

    @NotBlank(message = "Name is required")
    private String name;
    private String endpoint;
    private String iconUrl;
    private String description;
    private String displayName;
    private Boolean forwardAuthToken;
    private List<String> entities;
    private InterceptorSourceDto source;
    private String configurationEndpoint;
    private String author;
    @EqualsAndHashCode.Exclude
    private Instant createdAt;
    @EqualsAndHashCode.Exclude
    private Instant updatedAt;
    private List<String> dependencies;
}
