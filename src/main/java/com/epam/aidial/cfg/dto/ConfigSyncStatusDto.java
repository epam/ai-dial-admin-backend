package com.epam.aidial.cfg.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ConfigSyncStatusDto {

    private boolean isSuccess;
    private List<String> errors;
}
