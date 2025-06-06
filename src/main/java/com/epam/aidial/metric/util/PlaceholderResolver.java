package com.epam.aidial.metric.util;

import lombok.RequiredArgsConstructor;

import java.util.function.Function;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class PlaceholderResolver {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^:}]+)(?::([^}]*))?}");

    private final Function<String, String> envResolver;

    public String resolvePlaceholders(String input) {
        var matcher = PLACEHOLDER_PATTERN.matcher(input);
        var result = new StringBuilder();

        while (matcher.find()) {
            var envVar = matcher.group(1);
            var defaultValue = matcher.group(2) != null ? matcher.group(2) : "";
            var resolvedValue = envResolver.apply(envVar) != null ? envResolver.apply(envVar) : defaultValue;
            matcher.appendReplacement(result, resolvedValue);
        }
        matcher.appendTail(result);

        return result.toString();
    }

}
