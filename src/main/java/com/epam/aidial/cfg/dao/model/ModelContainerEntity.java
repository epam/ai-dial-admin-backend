package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Embeddable
public class ModelContainerEntity extends ContainerEntity {
    private String responsesEndpointPath;
}