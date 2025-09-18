package com.epam.aidial.cfg.domain.model;

import lombok.Data;

@Data
public class SecuredRoleBased implements DeploymentHolder {

    private SecuredResource deployment;
}
