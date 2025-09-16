package com.epam.aidial.cfg.domain.model;

import lombok.Data;

@Data
public class RoleBased implements DeploymentHolder {

    private Deployment deployment;
}
