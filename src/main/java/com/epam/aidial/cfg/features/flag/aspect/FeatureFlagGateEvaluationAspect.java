package com.epam.aidial.cfg.features.flag.aspect;


import com.epam.aidial.cfg.configuration.FeatureProperties;
import com.epam.aidial.cfg.features.flag.annotation.FeatureFlagGate;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Aspect
@Component
public class FeatureFlagGateEvaluationAspect {

    private final Map<String, Boolean> featureFlags;

    public FeatureFlagGateEvaluationAspect(FeatureProperties properties) {
        featureFlags = properties.getFlags();
    }

    @Before("@annotation(featureFlagGate)")
    public void evaluate(JoinPoint joinPoint, FeatureFlagGate featureFlagGate) {
        String featureFlag = featureFlagGate.featureFlag();
        if (Objects.equals(featureFlags.get(featureFlag), true)) {
            throw new UnsupportedOperationException("Feature flag '" + featureFlag + "' is disabled.");
        }
    }
}
