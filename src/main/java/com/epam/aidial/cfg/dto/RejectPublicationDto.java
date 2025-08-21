package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Data
public class RejectPublicationDto {
    @NotEmpty
    private String path;
    @Size(min = 15, max = 255, message = "Comment must be between 15 and 255 characters")
    private String comment;
}
