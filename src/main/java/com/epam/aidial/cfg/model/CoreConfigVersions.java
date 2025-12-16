package com.epam.aidial.cfg.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoreConfigVersions {

    private String autoDetectedVersion;
    private String defaultVersion;
    private String manuallySetVersion;

}