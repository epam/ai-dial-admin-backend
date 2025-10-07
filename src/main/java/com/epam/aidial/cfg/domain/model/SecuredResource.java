package com.epam.aidial.cfg.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SecuredResource extends Deployment {

    private ResourceAuthSettings authSettings;

    public SecuredResource(String name) {
        this.setName(name);
    }

    public SecuredResource(Deployment deployment, ResourceAuthSettings authSettings) {
        this.setName(deployment.getName());
        this.setRoleLimits(deployment.getRoleLimits());
        this.setIsPublic(deployment.getIsPublic());
        this.setDefaultRoleLimit(deployment.getDefaultRoleLimit());
        this.authSettings = authSettings;
    }
}
