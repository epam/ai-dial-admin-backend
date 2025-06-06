package com.epam.aidial.cfg.domain.model;

import lombok.Data;

@Data
public class RoleShareResourceLimit {

    private String role;
    private String deploymentName;

    private ShareResourceLimit limit;
}
