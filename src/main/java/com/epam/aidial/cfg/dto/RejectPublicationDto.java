package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class RejectPublicationDto {
    @NotEmpty
    private String path;
    private String comment;
}
