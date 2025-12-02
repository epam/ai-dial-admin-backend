package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.dto.validation.annotation.Endpoint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
public class AdapterDto {

    @NotEmpty
    private String name;
    @NotBlank(message = "DisplayName is required")
    private String displayName;
    @NotEmpty
    @Endpoint
    private String baseEndpoint;
    private String description;
    @EqualsAndHashCode.Exclude
    private Instant createdAt;
    @EqualsAndHashCode.Exclude
    private Instant updatedAt;
    private List<String> models = new ArrayList<>();
}
