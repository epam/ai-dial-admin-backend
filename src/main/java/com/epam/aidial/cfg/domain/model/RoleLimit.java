package com.epam.aidial.cfg.domain.model;

import lombok.Data;

@Data
public class RoleLimit {

    private String role;
    private String deploymentName;

    private boolean enabled = true;
    private Limit limit;
}
