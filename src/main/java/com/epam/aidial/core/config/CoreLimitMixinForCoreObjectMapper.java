package com.epam.aidial.core.config;

import com.fasterxml.jackson.annotation.JsonInclude;

public abstract class CoreLimitMixinForCoreObjectMapper {
    @JsonInclude
    private Long minute;
    @JsonInclude
    private Long day;
    @JsonInclude
    private Long week;
    @JsonInclude
    private Long month;
    @JsonInclude
    private Long requestHour;
    @JsonInclude
    private Long requestDay;
}
