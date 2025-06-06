package com.epam.aidial.cfg.domain.model;

import com.epam.aidial.cfg.dao.model.FeaturesEntity;
import lombok.Data;

@Data
public class AssistantsProperty {
    private String endpoint;
    private FeaturesEntity features;
}
