package com.epam.aidial.cfg.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConfigSyncStatusDto {

    private ConfigExportStatusDto configExportStatusDto;
    private ConfigReloadStatusDto configReloadStatusDto;
}
