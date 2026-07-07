package com.epam.aidial.cfg.model;

import lombok.Data;

@Data
public class ExternalServiceResource {

    private String displayName;
    private String description;
    private ResourceAuthSettings authSettings;
}
