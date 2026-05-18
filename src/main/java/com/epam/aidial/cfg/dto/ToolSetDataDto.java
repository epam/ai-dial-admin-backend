package com.epam.aidial.cfg.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ToolSetDataDto extends DeploymentDataDto {

    private String transport;
    private List<String> allowedTools;
    private ResourceAuthSettingsDto authSettings;
}
