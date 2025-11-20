package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class GlobalInterceptorsDto {
    @NotNull
    private List<String> globalInterceptorIds;
}