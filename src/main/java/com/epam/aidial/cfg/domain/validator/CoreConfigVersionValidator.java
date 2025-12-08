package com.epam.aidial.cfg.domain.validator;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class CoreConfigVersionValidator {

    public static final Pattern VERSION_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+$");
    public static final String LATEST_VERSION = "latest";

    public void validateVersionFormat(String version) {
        if (!VERSION_PATTERN.matcher(version).matches() && !LATEST_VERSION.equals(version)) {
            throw new IllegalArgumentException("Invalid version format: " + version
                    + ". Expected format: X.Y.Z (e.g., '0.23.0', '1.0.1', '2.0.3')) or 'latest'");
        }
    }
}
