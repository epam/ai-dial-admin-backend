package com.epam.aidial.cfg.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ModelData extends DeploymentData {

    private ModelCapabilitiesData capabilities;
    private String tokenizerModel;
    private ModelLimitsData limits;
    private ModelPricingData pricing;
    private Integer embeddingDimensions;
}
