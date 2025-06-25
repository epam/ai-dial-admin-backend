package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AdapterDto {

    @NotEmpty
    private String name;
    private String displayName;
    @NotEmpty
    private String baseEndpoint;
    private String description;
    private List<String> models = new ArrayList<>();
}
