package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class InterceptorContainerEntity {
    private String containerId;
    private String completionEndpointPath;
    private String configurationEndpointPath;
}