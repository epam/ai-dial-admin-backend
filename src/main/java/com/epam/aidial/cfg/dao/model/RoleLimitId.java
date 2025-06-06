package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Data
@Embeddable
public class RoleLimitId implements Serializable {

    @Column(name = "deployment_name", nullable = false)
    private String deploymentName;

    @Column(name = "role_name", nullable = false)
    private String roleName;

    public RoleLimitId() {
    }

    public RoleLimitId(String deploymentName, String roleName) {
        this.deploymentName = deploymentName;
        this.roleName = roleName;
    }

}
