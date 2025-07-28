package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(exclude = {"createdAt", "updatedAt"})
public class AdapterDto {

    @NotEmpty
    private String name;
    private String displayName;
    @NotEmpty
    private String baseEndpoint;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    private List<String> models = new ArrayList<>();
}
