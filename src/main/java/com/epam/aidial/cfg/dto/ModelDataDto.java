package com.epam.aidial.cfg.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ModelDataDto extends DeploymentDataDto {

    private ModelCapabilitiesDataDto capabilities;
    private String tokenizerModel;
    private ModelLimitsDataDto limits;
    private ModelPricingDataDto pricing;
}
