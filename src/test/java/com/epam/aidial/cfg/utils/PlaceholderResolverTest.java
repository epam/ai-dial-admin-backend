package com.epam.aidial.cfg.utils;

import com.epam.aidial.metric.util.PlaceholderResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaceholderResolverTest {

    @Mock
    private Function<String, String> envResolver;

    @InjectMocks
    private PlaceholderResolver tested;

    @Test
    public void testResolvePlaceholders_withEnvVar() {
        when(envResolver.apply("METRICS_CONFIG_FILE")).thenReturn("env/metric.config.json");

        var input = "Path to metrics config: ${METRICS_CONFIG_FILE:data/admin/metric.config.json}";
        var expected = "Path to metrics config: env/metric.config.json";
        var result = tested.resolvePlaceholders(input);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testResolvePlaceholders_withDefaultValue() {
        when(envResolver.apply("METRICS_CONFIG_FILE")).thenReturn(null);

        var input = "Path to metrics config: ${METRICS_CONFIG_FILE:data/admin/metric.config.json}";
        var expected = "Path to metrics config: data/admin/metric.config.json";
        var result = tested.resolvePlaceholders(input);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testResolvePlaceholders_withEmptyValue() {
        when(envResolver.apply("METRICS_CONFIG_FILE")).thenReturn(null);

        var input = "Path to metrics config: ${METRICS_CONFIG_FILE}";
        var expected = "Path to metrics config: ";
        var result = tested.resolvePlaceholders(input);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testResolvePlaceholders_withMultiplePlaceholders() {
        when(envResolver.apply("METRICS_CONFIG_FILE")).thenReturn("env/metric.config.json");
        when(envResolver.apply("ANOTHER_VAR")).thenReturn("anotherValue");

        var input = "Config: ${METRICS_CONFIG_FILE:data/admin/metric.config.json}, Another: ${ANOTHER_VAR:defaultValue}";
        var expected = "Config: env/metric.config.json, Another: anotherValue";
        var result = tested.resolvePlaceholders(input);

        assertThat(result).isEqualTo(expected);
    }

}