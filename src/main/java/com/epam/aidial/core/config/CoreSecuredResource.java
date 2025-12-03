package com.epam.aidial.core.config;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class CoreSecuredResource extends Deployment {

    @JsonAlias({"authSettings", "auth_settings"})
    protected CoreResourceAuthSettings authSettings = new CoreResourceAuthSettings();
}