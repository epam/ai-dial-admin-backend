package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class FilePathsDto {
    @NotEmpty
    private List<FilePathDto> paths;
}