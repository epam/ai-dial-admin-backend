package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import org.hibernate.envers.Audited;

@Data
@Audited
@MappedSuperclass
public abstract class ContainerEntity {
    private String containerId;
    private String containerName;
    private String completionEndpointPath;
}
