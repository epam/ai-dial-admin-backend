package com.epam.aidial.core.config;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

class CoreObjectsEmptyMethodContractTest {

    @Test
    void coreModelEmpty_shouldHaveAllFieldsNull() throws IllegalAccessException {
        assertAllFieldsNull(CoreModel.empty());
    }

    @Test
    void coreToolSetEmpty_shouldHaveAllFieldsNull() throws IllegalAccessException {
        assertAllFieldsNull(CoreToolSet.empty());
    }

    @Test
    void coreResourceAuthSettingsEmpty_shouldHaveAllFieldsNull() throws IllegalAccessException {
        assertAllFieldsNull(CoreResourceAuthSettings.empty());
    }

    private void assertAllFieldsNull(Object instance) throws IllegalAccessException {
        List<String> nonNullFields = new ArrayList<>();
        Class<?> clazz = instance.getClass();

        while (clazz != null && clazz != Object.class) {
            populateNonNullFields(instance, clazz, nonNullFields);
            clazz = clazz.getSuperclass();
        }

        String className = instance.getClass().getSimpleName();
        Assertions.assertThat(nonNullFields)
                .as("%s.empty() must set every field to null — add setXxx(null) calls for the following fields: %s"
                        .formatted(className, nonNullFields))
                .isEmpty();
    }

    private void populateNonNullFields(Object instance, Class<?> clazz, List<String> nonNullFields) throws IllegalAccessException {
        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                continue;
            }

            field.setAccessible(true);
            if (field.get(instance) != null) {
                nonNullFields.add(clazz.getSimpleName() + "." + field.getName());
            }
        }
    }
}