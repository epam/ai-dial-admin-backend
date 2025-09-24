package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.MappedSuperclass;
import lombok.Data;

@Data
@MappedSuperclass
public abstract class ContainerEntity {
    private String containerId;
    private String containerName;
    private String completionEndpointPath;
}
