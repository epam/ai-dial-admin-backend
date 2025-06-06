package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.exception.ValidationException;
import com.epam.aidial.core.config.CoreModel;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.epam.aidial.cfg.service.Assertions.assertUniqueDisplayName;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssertionsTest {

    @Test
    void testAssertUniqueDisplayName_DisplayNameEmpty() {
        // given
        CoreModel model = new CoreModel();
        model.setDisplayName("testModel");
        // when
        assertUniqueDisplayName(Map.of("testModel", model), null);
        // then
    }

    @Test
    void testAssertUniqueDisplayName_NewDisplayName() {
        // given
        CoreModel model = new CoreModel();
        model.setDisplayName("testModel");
        // when
        assertUniqueDisplayName(Map.of("testModel", model), "newDisplayName");
        // then
    }

    @Test
    void testAssertUniqueDisplayName_DisplayNameIsNotUnique() {
        // given
        CoreModel model = new CoreModel();
        model.setDisplayName("testModel");
        Map<String, CoreModel> models = Map.of("testModel", model);
        // when
        assertThatThrownBy(() -> assertUniqueDisplayName(models, "testModel"))
                // then
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("displayName is not unique");
    }

    @Test
    void testAssertUniqueDisplayNameAndVersion_DisplayNameEmptyAndDisplayVersionEmpty() {
        // given
        CoreModel model = new CoreModel();
        model.setDisplayName("testModel");
        model.setDisplayVersion("1.0.0");
        Map<String, CoreModel> models = Map.of("testModel", model);
        // when
        Assertions.assertUniqueDisplayNameAndVersion(models, null, null);
        // then
    }

    @Test
    void testAssertUniqueDisplayNameAndVersion_DisplayNameEmptyAndDisplayVersionNotEmpty_Exception() {
        // given
        CoreModel model = new CoreModel();
        model.setDisplayVersion("1.0.0");
        Map<String, CoreModel> models = Map.of("testModel", model);
        // when
        assertThatThrownBy(() -> Assertions.assertUniqueDisplayNameAndVersion(models, null, "1.0.0"))
                // then
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("displayVersion is not unique");
    }

    @Test
    void testAssertUniqueDisplayNameAndVersion_DisplayNameEmptyAndDisplayVersionNotEmpty_Success() {
        // given
        CoreModel model = new CoreModel();
        model.setDisplayVersion("1.0.0");
        Map<String, CoreModel> models = Map.of("testModel", model);
        // when
        Assertions.assertUniqueDisplayNameAndVersion(models, null, "2.0.0");
        // then
    }

    @Test
    void testAssertUniqueDisplayNameAndVersion_DisplayNameNotEmptyAndDisplayVersionEmpty_Exception() {
        // given
        CoreModel model = new CoreModel();
        model.setDisplayName("testModel");
        Map<String, CoreModel> models = Map.of("testModel", model);
        // when
        assertThatThrownBy(() -> Assertions.assertUniqueDisplayNameAndVersion(models, "testModel", null))
                // then
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("displayName is not unique");
    }

    @Test
    void testAssertUniqueDisplayNameAndVersion_DisplayNameNotEmptyAndDisplayVersionEmpty_Success() {
        // given
        CoreModel model = new CoreModel();
        model.setDisplayName("testModel");
        model.setDisplayVersion("1.0.0");
        Map<String, CoreModel> models = Map.of("testModel", model);
        // when
        Assertions.assertUniqueDisplayNameAndVersion(models, "testModel", null);
        // then
    }

    @Test
    void testAssertUniqueDisplayNameAndVersion_DisplayNameNotEmptyAndDisplayVersionNotEmpty_Exception() {
        // given
        CoreModel model = new CoreModel();
        model.setDisplayName("testModel");
        model.setDisplayVersion("1.0.0");
        Map<String, CoreModel> models = Map.of("testModel", model);
        // when
        assertThatThrownBy(() -> Assertions.assertUniqueDisplayNameAndVersion(models, "testModel", "1.0.0"))
                // then
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("displayName and displayVersion are not unique");
    }

    @Test
    void testAssertUniqueDisplayNameAndVersion_DisplayNameNotEmptyAndDisplayVersionNotEmpty_Success() {
        // given
        CoreModel model = new CoreModel();
        model.setDisplayName("testModel");
        model.setDisplayVersion("1.0.0");
        Map<String, CoreModel> models = Map.of("testModel", model);
        // when
        Assertions.assertUniqueDisplayNameAndVersion(models, "testModel", "2.0.0");
        // then
    }
}