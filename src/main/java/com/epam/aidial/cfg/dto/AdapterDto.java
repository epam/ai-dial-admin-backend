package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.dto.source.AdapterSourceDto;
import com.epam.aidial.cfg.dto.validation.annotation.Endpoint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

@Data
public class AdapterDto {

    @NotEmpty
    private String name;
    @NotBlank(message = "DisplayName is required")
    private String displayName;
    @Endpoint
    private String baseEndpoint;
    @Endpoint
    private String responsesEndpoint;
    private String description;
    @EqualsAndHashCode.Exclude
    private Instant createdAt;
    @EqualsAndHashCode.Exclude
    private Instant updatedAt;
    private List<String> models = new ArrayList<>();
    private TreeSet<String> topics;
    private AdapterSourceDto source;
}