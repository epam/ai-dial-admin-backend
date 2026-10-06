package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Pricing;
import com.epam.aidial.cfg.domain.model.PricingRate;
import com.epam.aidial.cfg.domain.model.PricingRateCondition;
import com.epam.aidial.cfg.domain.model.PricingRateOperator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PricingValidatorTest {

    private static final String MODEL = "model";
    private static final String UNIT = "char_without_whitespace";
    private static final String CACHE_READ_MESSAGE = "'cacheRead' is not allowed for unit 'char_without_whitespace'";
    private static final String CACHE_WRITE_MESSAGE = "'cacheWrite' is not allowed for unit 'char_without_whitespace'";
    private static final String PROMPT_MESSAGE = "'prompt' must be a plain numeric rate";
    private static final String NULL_UNIT_MESSAGE = "Pricing 'unit' must not be null";
    private static final String BLANK_PROMPT_MESSAGE = "'prompt' must not be empty or blank";
    private static final String BLANK_COMPLETION_MESSAGE = "'completion' must not be empty or blank";
    private static final String COMPLETION_MESSAGE = "'completion' must be a plain numeric rate";

    private final PricingValidator validator = new PricingValidator();

    @ParameterizedTest
    @ValueSource(strings = {"0.000004", "0.000002", "0.001"})
    void validate_shouldAcceptPlainRates(String rate) {
        // given
        Pricing pricing = pricing(rate, rate);

        // when / then
        assertThatNoException().isThrownBy(() -> validator.validate(pricing, MODEL));
    }

    @Test
    void validate_shouldRejectPricingWithCacheRead() {
        // given
        Pricing pricing = pricing("0.001", "0.002");
        pricing.setCacheRead(leaf("0.0001"));

        // when / then
        assertThatThrownBy(() -> validator.validate(pricing, MODEL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(CACHE_READ_MESSAGE);
    }

    @Test
    void validate_shouldRejectPricingWithCacheWrite() {
        // given
        Pricing pricing = pricing("0.001", "0.002");
        pricing.setCacheWrite(leaf("0.0001"));

        // when / then
        assertThatThrownBy(() -> validator.validate(pricing, MODEL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(CACHE_WRITE_MESSAGE);
    }

    @Test
    void validate_shouldRejectPricingWithCacheDecisionTree() {
        // given
        Pricing pricing = pricing("0.001", "0.002");
        pricing.setCacheRead(tree());

        // when / then
        assertThatThrownBy(() -> validator.validate(pricing, MODEL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(CACHE_READ_MESSAGE);
    }

    @Test
    void validate_shouldRejectDecisionTreeInPrompt() {
        // given
        Pricing pricing = pricing("0.001", "0.002");
        pricing.setPrompt(tree());

        // when / then
        assertThatThrownBy(() -> validator.validate(pricing, MODEL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(PROMPT_MESSAGE);
    }

    @Test
    void validate_shouldRejectDecisionTreeInCompletion() {
        // given
        Pricing pricing = pricing("0.001", "0.002");
        pricing.setCompletion(tree());

        // when / then
        assertThatThrownBy(() -> validator.validate(pricing, MODEL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(COMPLETION_MESSAGE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "1.2.3", "NaN", "Infinity"})
    void validate_shouldRejectNonNumericPromptRate(String rate) {
        // given
        Pricing pricing = pricing(rate, "0.002");

        // when / then
        assertThatThrownBy(() -> validator.validate(pricing, MODEL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(PROMPT_MESSAGE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "1.2.3"})
    void validate_shouldRejectNonNumericCompletionRate(String rate) {
        // given
        Pricing pricing = pricing("0.001", rate);

        // when / then
        assertThatThrownBy(() -> validator.validate(pricing, MODEL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(COMPLETION_MESSAGE);
    }

    @Test
    void validate_shouldNotRestrictTokenUnit() {
        // given
        Pricing pricing = new Pricing();
        pricing.setUnit("token");
        pricing.setPrompt(tree());
        pricing.setCompletion(tree());
        pricing.setCacheRead(tree());
        pricing.setCacheWrite(leaf("0.1"));

        // when / then
        assertThatNoException().isThrownBy(() -> validator.validate(pricing, MODEL));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   "})
    void validate_shouldRejectEmptyOrBlankPromptRate(String blank) {
        // given
        Pricing nonToken = pricing(blank, "0.002");
        Pricing token = pricing(blank, "0.002");
        token.setUnit("token");

        // when / then
        assertThatThrownBy(() -> validator.validate(nonToken, MODEL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(BLANK_PROMPT_MESSAGE);
        assertThatThrownBy(() -> validator.validate(token, MODEL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(BLANK_PROMPT_MESSAGE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   "})
    void validate_shouldRejectEmptyOrBlankCompletionRate(String blank) {
        // given
        Pricing nonToken = pricing("0.001", blank);
        Pricing token = pricing("0.001", blank);
        token.setUnit("token");

        // when / then
        assertThatThrownBy(() -> validator.validate(nonToken, MODEL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(BLANK_COMPLETION_MESSAGE);
        assertThatThrownBy(() -> validator.validate(token, MODEL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(BLANK_COMPLETION_MESSAGE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void validate_shouldRejectBlankRateInsideTokenDecisionTreeAndCache(String blank) {
        // given
        Pricing treeWithBlankBranch = pricing("0.001", "0.002");
        treeWithBlankBranch.setUnit("token");
        PricingRate tree = tree();
        tree.setIfFalse(leaf(blank));
        treeWithBlankBranch.setPrompt(tree);

        Pricing blankCacheRead = pricing("0.001", "0.002");
        blankCacheRead.setUnit("token");
        blankCacheRead.setCacheRead(leaf(blank));

        // when / then
        assertThatThrownBy(() -> validator.validate(treeWithBlankBranch, MODEL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(BLANK_PROMPT_MESSAGE);
        assertThatThrownBy(() -> validator.validate(blankCacheRead, MODEL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("'cacheRead' must not be empty or blank");
    }

    @Test
    void validate_shouldRejectNullUnit() {
        // given
        Pricing pricing = pricing("0.001", "0.002");
        pricing.setUnit(null);

        // when / then
        assertThatThrownBy(() -> validator.validate(pricing, MODEL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(NULL_UNIT_MESSAGE);
    }

    @Test
    void validate_shouldRejectNullUnitWithCacheAndTree() {
        // given
        Pricing pricing = new Pricing();
        pricing.setPrompt(tree());
        pricing.setCacheRead(leaf("0.1"));
        pricing.setCacheWrite(tree());

        // when / then
        assertThatThrownBy(() -> validator.validate(pricing, MODEL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(NULL_UNIT_MESSAGE);
    }

    @Test
    void validate_shouldRejectNullUnitWithoutRates() {
        // when / then
        assertThatThrownBy(() -> validator.validate(new Pricing(), MODEL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(NULL_UNIT_MESSAGE);
    }

    @Test
    void validate_shouldAcceptNullPricing() {
        // when / then
        assertThatNoException().isThrownBy(() -> validator.validate(null, MODEL));
    }

    @Test
    void validate_shouldAcceptUnitWithoutRates() {
        // given
        Pricing pricing = new Pricing();
        pricing.setUnit("none");

        // when / then
        assertThatNoException().isThrownBy(() -> validator.validate(pricing, MODEL));
    }

    private static Pricing pricing(String prompt, String completion) {
        Pricing pricing = new Pricing();
        pricing.setUnit(UNIT);
        pricing.setPrompt(leaf(prompt));
        pricing.setCompletion(leaf(completion));
        return pricing;
    }

    private static PricingRate leaf(String rate) {
        return PricingRate.flat(rate);
    }

    private static PricingRate tree() {
        PricingRateCondition condition = new PricingRateCondition();
        condition.setField("prompt_tokens");
        condition.setOperator(PricingRateOperator.GT);
        condition.setValue(1000);
        PricingRate node = new PricingRate();
        node.setTest(condition);
        node.setIfTrue(leaf("0.001"));
        node.setIfFalse(leaf("0.002"));
        return node;
    }
}
