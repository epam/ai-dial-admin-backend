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

        validateNotBlank(pricing.getPrompt(), "prompt", entityName);
        validateNotBlank(pricing.getCompletion(), "completion", entityName);
        validateNotBlank(pricing.getCacheRead(), "cacheRead", entityName);
        validateNotBlank(pricing.getCacheWrite(), "cacheWrite", entityName);

        if (TOKEN_UNIT.equals(pricing.getUnit())) {
            return;
        }

        if (pricing.getCacheRead() != null) {
            throw new IllegalArgumentException(
                    "Pricing 'cacheRead' is not allowed for unit '%s'. Model: %s".formatted(pricing.getUnit(), entityName));
        }
        if (pricing.getCacheWrite() != null) {
            throw new IllegalArgumentException(
                    "Pricing 'cacheWrite' is not allowed for unit '%s'. Model: %s".formatted(pricing.getUnit(), entityName));
        }
        validatePlainRate(pricing.getPrompt(), "prompt", pricing.getUnit(), entityName);
        validatePlainRate(pricing.getCompletion(), "completion", pricing.getUnit(), entityName);
    }

    /**
     * Rejects an empty or blank rate anywhere in the rate, including all branches of a decision tree.
     */
    private void validateNotBlank(PricingRate rate, String field, String entityName) {
        if (rate == null) {
            return;
        }
        if (rate.isLeaf()) {
            if (StringUtils.isBlank(rate.getRate())) {
                throw new IllegalArgumentException(
                        "Pricing '%s' must not be empty or blank. Model: %s".formatted(field, entityName));
            }
            return;
        }
        validateNotBlank(rate.getIfTrue(), field, entityName);
        validateNotBlank(rate.getIfFalse(), field, entityName);
    }

    private void validatePlainRate(PricingRate rate, String field, String unit, String entityName) {
        if (rate == null) {
            return;
        }
        if (!rate.isLeaf()) {
            throw invalidRate(field, "decision tree", unit, entityName);
        }
        double value;
        try {
            value = Double.parseDouble(rate.getRate());
        } catch (NumberFormatException e) {
            throw invalidRate(field, rate.getRate(), unit, entityName);
        }
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw invalidRate(field, rate.getRate(), unit, entityName);
        }
    }

    private IllegalArgumentException invalidRate(String field, String rate, String unit, String entityName) {
        return new IllegalArgumentException(
                "Pricing '%s' must be a plain numeric rate (decision tree is not allowed) for unit '%s', got '%s'. Model: %s"
                        .formatted(field, unit, rate, entityName));
    }
}
