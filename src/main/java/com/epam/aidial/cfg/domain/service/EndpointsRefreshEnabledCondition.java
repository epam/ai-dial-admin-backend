package com.epam.aidial.cfg.domain.service;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class EndpointsRefreshEnabledCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, @NotNull AnnotatedTypeMetadata metadata) {
        final String refreshEnabled = context.getEnvironment()
                .getProperty("plugins.deployment.manager.endpoint.refresh.enabled", "false");
        return Boolean.parseBoolean(refreshEnabled);
    }
}
