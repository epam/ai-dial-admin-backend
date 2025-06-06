package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class AdapterDto {

    @NotEmpty
    private String name;
    @NotEmpty
    private String baseEndpoint;
}
