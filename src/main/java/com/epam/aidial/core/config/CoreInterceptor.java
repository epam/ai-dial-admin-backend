package com.epam.aidial.core.config;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CoreInterceptor extends Deployment {
    @JsonAlias({"configurationEndpoint", "configuration_endpoint"})
    private String configurationEndpoint;
}
