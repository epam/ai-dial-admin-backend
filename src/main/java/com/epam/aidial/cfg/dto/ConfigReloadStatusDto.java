package com.epam.aidial.cfg.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConfigReloadStatusDto {

    private boolean isSuccess;
    private String errorMessage;
}
