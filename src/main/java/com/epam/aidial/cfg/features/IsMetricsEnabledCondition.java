package com.epam.aidial.cfg.features;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class IsMetricsEnabledCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, @NotNull AnnotatedTypeMetadata metadata) {
        return context.getEnvironment().getProperty("metrics.enabled", Boolean.class, false);
    }
}
