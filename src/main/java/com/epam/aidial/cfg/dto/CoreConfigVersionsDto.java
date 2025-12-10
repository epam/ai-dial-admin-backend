package com.epam.aidial.cfg.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoreConfigVersionsDto {

    private String autoDetectedVersion;
    private String defaultVersion;
    private String manuallySetVersion;

}