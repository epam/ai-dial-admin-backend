package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.exception.ValidationException;
import com.epam.aidial.core.config.Deployment;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;

public class Assertions {

    public static void assertUniqueDisplayName(Map<String, ? extends Deployment> deployments, String newDisplayName) {
        if (StringUtils.isEmpty(newDisplayName)) {
            return;
        }
        for (Map.Entry<String, ? extends Deployment> entry : deployments.entrySet()) {
            String entityName = entry.getKey();
            Deployment deployment = entry.getValue();
            String currentValue = deployment.getDisplayName();
            if (StringUtils.isNotEmpty(currentValue) && Objects.equals(newDisplayName, currentValue)) {
                throw new ValidationException("displayName is not unique. displayName '" + newDisplayName + "' already exists (" + entityName + ")");
            }
        }
    }

    public static void assertUniqueDisplayNameAndVersion(Map<String, ? extends Deployment> deployments, String newDisplayName, String newDisplayVersion) {
        if (StringUtils.isEmpty(newDisplayName) && StringUtils.isEmpty(newDisplayVersion)) {
            return;
        }

        for (Map.Entry<String, ? extends Deployment> entry : deployments.entrySet()) {
            String entityName = entry.getKey();
            Deployment deployment = entry.getValue();

            String displayName = deployment.getDisplayName();
            String displayVersion = deployment.getDisplayVersion();

            boolean isEqualDisplayNameAndDisplayVersion = isEqualDisplayNameAndDisplayVersion(displayName, displayVersion, newDisplayName, newDisplayVersion);

            if (StringUtils.isEmpty(newDisplayName) && isEqualDisplayNameAndDisplayVersion) {
                throw new ValidationException("displayVersion is not unique. "
                        + "displayVersion '" + newDisplayVersion + "' already exists (" + entityName + "). "
                        + "Change or add displayName.");
            }
            if (StringUtils.isEmpty(newDisplayVersion) && isEqualDisplayNameAndDisplayVersion) {
                throw new ValidationException("displayName is not unique. "
                        + "displayName '" + newDisplayName + "' already exists (" + entityName + "). "
                        + "Change or add displayVersion.");
            }
            if (isEqualDisplayNameAndDisplayVersion) {
                throw new ValidationException("displayName and displayVersion are not unique. "
                        + "displayName '" + newDisplayName + "' and displayVersion '" + newDisplayVersion + "' already exists (" + entityName + ")");
            }
        }
    }

    private static boolean isEqualDisplayNameAndDisplayVersion(String displayName,
                                                               String displayVersion,
                                                               String newDisplayName,
                                                               String newDisplayVersion) {
        return Objects.equals(displayName, newDisplayName) && Objects.equals(displayVersion, newDisplayVersion);
    }

}
