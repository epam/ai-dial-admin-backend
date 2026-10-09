package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Pricing;
import com.epam.aidial.cfg.domain.model.PricingRate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Validates model pricing. {@code unit} is required, and no rate may be empty or blank. For any unit other than
 * {@code token} cache rates and decision trees are rejected, and {@code prompt}/{@code completion} must be a
 * plain rate parseable as a double.
 */
@Component
public class PricingValidator {

    private static final String TOKEN_UNIT = "token";

    public void validate(Pricing pricing, String entityName) {
        if (pricing == null) {
            return;
        }
        if (pricing.getUnit() == null) {
            throw new IllegalArgumentException("Pricing 'unit' must not be null. Model: %s".formatted(entityName));
        }

        boolean isTokenUnit = TOKEN_UNIT.equals(pricing.getUnit());

        // Validate all rates
        validateRate(pricing.getPrompt(), "prompt", isTokenUnit, pricing.getUnit(), entityName);
        validateRate(pricing.getCompletion(), "completion", isTokenUnit, pricing.getUnit(), entityName);
        validateRate(pricing.getCacheRead(), "cacheRead", isTokenUnit, pricing.getUnit(), entityName);
        validateRate(pricing.getCacheWrite(), "cacheWrite", isTokenUnit, pricing.getUnit(), entityName);

        // Cache rates only allowed for token unit
        if (!isTokenUnit) {
            if (pricing.getCacheRead() != null) {
                throw new IllegalArgumentException(
                        "Pricing 'cacheRead' is not allowed for unit '%s'. Model: %s".formatted(pricing.getUnit(), entityName));
            }
            if (pricing.getCacheWrite() != null) {
                throw new IllegalArgumentException(
                        "Pricing 'cacheWrite' is not allowed for unit '%s'. Model: %s".formatted(pricing.getUnit(), entityName));
            }
        }
    }

    /**
     * Validates a PricingRate field according to the unit type and rules.
     */
    private void validateRate(PricingRate rate, String field, boolean isTokenUnit, String unit, String entityName) {
        if (rate == null) {
            return;
        }

        if (rate.isLeaf()) {
            // Validate leaf: must not be blank and must be a finite double
            if (StringUtils.isBlank(rate.getRate())) {
                throw new IllegalArgumentException(
                        "Pricing '%s' must not be empty or blank. Model: %s".formatted(field, entityName));
            }
            validateFiniteDouble(rate.getRate(), field, entityName);
        } else {
            // Decision tree validation
            if (!isTokenUnit) {
                throw new IllegalArgumentException(
                        "Pricing '%s' must be a plain numeric rate (decision tree is not allowed) for unit '%s'. Model: %s"
                                .formatted(field, unit, entityName));
            }
            validateDecisionTree(rate, field, entityName);
        }
    }

    /**
     * Validates that a rate string is a finite double value.
     */
    private void validateFiniteDouble(String rateStr, String field, String entityName) {
        double value;
        try {
            value = Double.parseDouble(rateStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Pricing '%s' must be a valid numeric rate, got '%s'. Model: %s"
                            .formatted(field, rateStr, entityName));
        }

        if (Double.isNaN(value)) {
            throw new IllegalArgumentException(
                    "Pricing '%s' must be a finite number, got 'NaN'. Model: %s"
                            .formatted(field, entityName));
        }

        if (Double.isInfinite(value)) {
            throw new IllegalArgumentException(
                    "Pricing '%s' must be a finite number, got 'Infinity'. Model: %s"
                            .formatted(field, entityName));
        }
    }

    /**
     * Validates a decision tree structure recursively.
     */
    private void validateDecisionTree(PricingRate rate, String field, String entityName) {
        // Recursively validate branches
        if (rate.getIfTrue() != null) {
            validateRate(rate.getIfTrue(), field, true, TOKEN_UNIT, entityName);
        }
        if (rate.getIfFalse() != null) {
            validateRate(rate.getIfFalse(), field, true, TOKEN_UNIT, entityName);
        }
    }
}
