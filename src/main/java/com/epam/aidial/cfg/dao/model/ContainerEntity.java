package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Audited
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class ContainerEntity {
    private String containerId;
    private String containerName;
    private String completionEndpointPath;
}
