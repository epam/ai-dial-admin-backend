package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.LocalizedValue;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DisplayNameUniquenessValidatorTest {

    private final DisplayNameUniquenessValidator validator = new DisplayNameUniquenessValidator();

    @Test
    void testPlainDisplayName_Unique() {
        Map<String, LocalizedValue> candidates = Map.of("existing", LocalizedValue.of("existing display"));

        assertThatCode(() -> validator.validateUnique("Model", null, LocalizedValue.of("new display"), null, candidates))
                .doesNotThrowAnyException();
    }

    @Test
    void testPlainDisplayName_Duplicate_Throws() {
        Map<String, LocalizedValue> candidates = Map.of("existing", LocalizedValue.of("display"));

        assertThatThrownBy(() -> validator.validateUnique("Model", null, LocalizedValue.of("display"), null, candidates))
                .isInstanceOf(EntityAlreadyExistsException.class)
                .hasMessageContaining("Model with display name");
    }

    @Test
    void testLocalizedDisplayName_OneLocale_Duplicate_Throws() {
        Map<String, LocalizedValue> candidates = Map.of("existing", LocalizedValue.of(Map.of("en", "en display")));

        assertThatThrownBy(() -> validator.validateUnique("Model", null, LocalizedValue.of(Map.of("en", "en display")), null, candidates))
                .isInstanceOf(EntityAlreadyExistsException.class);
    }

    @Test
    void testLocalizedDisplayName_MultipleLocales_Unique() {
        Map<String, LocalizedValue> candidates = Map.of(
                "existing", LocalizedValue.of(Map.of("en", "en display", "fr", "fr display")));
        LocalizedValue newDisplayName = LocalizedValue.of(Map.of("en", "en display new", "fr", "fr display new"));

        assertThatCode(() -> validator.validateUnique("Model", null, newDisplayName, null, candidates))
                .doesNotThrowAnyException();
    }

    @Test
    void testTaskExample_ConflictOnlyOnFrLocale() {
        // 1. {"en": "en display", "fr": "fr display"}
        // 3. {"en": "en display 1", "fr": "fr display"} -- conflicts with 1. on 'fr'
        Map<String, LocalizedValue> candidates = Map.of(
                "entity1", LocalizedValue.of(Map.of("en", "en display", "fr", "fr display")));

        LocalizedValue entity3 = LocalizedValue.of(Map.of("en", "en display 1", "fr", "fr display"));
        assertThatThrownBy(() -> validator.validateUnique("Model", null, entity3, null, candidates))
                .isInstanceOf(EntityAlreadyExistsException.class);
    }

    @Test
    void testTaskExample_PlainValueDoesNotConflict() {
        Map<String, LocalizedValue> candidates = Map.of(
                "entity1", LocalizedValue.of(Map.of("en", "en display", "fr", "fr display")),
                "entity3", LocalizedValue.of(Map.of("en", "en display 1", "fr", "fr display")));

        assertThatCode(() -> validator.validateUnique("Model", null, LocalizedValue.of("display"), null, candidates))
                .doesNotThrowAnyException();
    }

    @Test
    void testMissingValueForLocale_NoFalseConflict() {
        Map<String, LocalizedValue> candidates = Map.of("existing", LocalizedValue.of(Map.of("en", "en display")));
        LocalizedValue newDisplayName = LocalizedValue.of(Map.of("fr", "fr display"));

        assertThatCode(() -> validator.validateUnique("Model", null, newDisplayName, null, candidates))
                .doesNotThrowAnyException();
    }

    @Test
    void testSameValueInDifferentLocales_Allowed() {
        Map<String, LocalizedValue> candidates = Map.of("existing", LocalizedValue.of(Map.of("en", "shared value")));
        LocalizedValue newDisplayName = LocalizedValue.of(Map.of("fr", "shared value"));

        assertThatCode(() -> validator.validateUnique("Model", null, newDisplayName, null, candidates))
                .doesNotThrowAnyException();
    }

    @Test
    void testConflictBetweenPlainAndLocalized_Throws() {
        Map<String, LocalizedValue> candidates = Map.of(
                "existing", LocalizedValue.of(Map.of("en", "en display", "fr", "shared value")));

        assertThatThrownBy(() -> validator.validateUnique("Model", null, LocalizedValue.of("shared value"), null, candidates))
                .isInstanceOf(EntityAlreadyExistsException.class);
    }

    @Test
    void testSelfExclusionOnUpdate_NoThrow() {
        LocalizedValue displayName = LocalizedValue.of("unchanged display");
        Map<String, LocalizedValue> candidates = Map.of("self", displayName);

        assertThatCode(() -> validator.validateUnique("Model", "self", displayName, "1.0.0", candidates))
                .doesNotThrowAnyException();
    }

    @Test
    void testBlankNewDisplayNameAndVersion_SkipsValidation() {
        Map<String, LocalizedValue> candidates = Map.of("existing", LocalizedValue.of("existing display"));

        assertThatCode(() -> validator.validateUnique("Model", null, null, null, candidates))
                .doesNotThrowAnyException();
    }
}
