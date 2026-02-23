package com.epam.aidial.cfg.utils;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class MapExtractionUtilsTest {
    @Test
    void shouldReturnEmptyWhenKeysIsNull() {
        var result = MapExtractionUtils.extractFirstPresentValue(Map.of("key", "value"), null);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenKeysIsEmpty() {
        var result = MapExtractionUtils.extractFirstPresentValue(Map.of("key", "value"), List.of());
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnFirstMatchingValue() {
        var source = Map.of("email", "test@mail.com", "username", "user1");
        var result = MapExtractionUtils.extractFirstPresentValue(source, List.of("username", "email"));
        assertThat(result).contains("user1");
    }

    @Test
    void shouldSkipNullValues() {
        var source = Map.of("email", "test@mail.com");
        var result = MapExtractionUtils.extractFirstPresentValue(source, List.of("username", "email"));
        assertThat(result).contains("test@mail.com");
    }

    @Test
    void shouldReturnEmptyWhenNoKeysMatch() {
        var source = Map.of("email", "test@mail.com");
        var result = MapExtractionUtils.extractFirstPresentValue(source, List.of("role", "id"));
        assertThat(result).isEmpty();
    }
}