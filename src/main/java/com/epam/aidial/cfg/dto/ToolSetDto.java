package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ToolSetDto extends RoleBasedDto {

    @NotBlank(message = "Name is required")
    private String name;
    @NotNull
    private TransportDto transport;
    private List<String> allowedTools = List.of();

    @EqualsAndHashCode.Exclude
    private Instant createdAt;
    @EqualsAndHashCode.Exclude
    private Instant updatedAt;

    public enum TransportDto {
        HTTP, SSE
    }
}
