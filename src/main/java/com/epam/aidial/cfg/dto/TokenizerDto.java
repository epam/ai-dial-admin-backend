package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenizerDto {

    @NotEmpty
    private String name;
    @NotEmpty
    private String id;
    private String description;
}
