package com.epam.aidial.cfg.domain.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@ToString(exclude = "roleLimits")
@NoArgsConstructor(access = AccessLevel.PACKAGE) // for tests
public class Deployment {
    private String name;
    private List<RoleLimit> roleLimits;
    private List<RoleShareResourceLimit> roleShareResourceLimits;
    private Boolean isPublic = false;
    private Limit defaultRoleLimit;
    private ShareResourceLimit defaultRoleShareResourceLimit;

    public Deployment(String name) {
        this.name = name;
    }
}
