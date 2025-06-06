package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.exception.ValidationException;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreRole;
import com.epam.aidial.core.config.Deployment;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class Assertions {

    public static void assertUnique(Config config, String name) {
        Map<String, Map<String, ?>> entities = Map.of(
                "Addon", config.getAddons(),
                "Assistant", config.getAssistant().getAssistants(),
                "Application", config.getApplications(),
                "Model", config.getModels(),
                "Key", config.getKeys(),
                "Role", config.getRoles(),
                "Interceptor", config.getInterceptors(),
                "Route", config.getRoutes()
        );
        for (Map.Entry<String, Map<String, ?>> entry : entities.entrySet()) {
            String entityName = entry.getKey();
            Map<String, ?> entityMap = entry.getValue();

            if (entityMap.containsKey(name)) {
                throw new EntityAlreadyExistsException("Name is not unique. " + entityName + " with name " + name + " already exists");
            }
        }
    }


    public static void assertKeyExists(Config config, String key) {
        if (!config.getKeys().containsKey(key)) {
            throw new EntityNotFoundException("Can't find Key with name=" + key);
        }
    }

    public static void assertKeyNotExists(Config config, String key) {
        if (config.getKeys().containsKey(key)) {
            throw new EntityAlreadyExistsException("Key with name already exists=" + key);
        }
    }

    public static void assertRoleExists(Config config, String roleName) {
        if (!config.getRoles().containsKey(roleName)) {
            throw new EntityNotFoundException("Can't find Role Limits with Role name=" + roleName);
        }
    }

    public static void assertRoleNotExists(Config config, String roleName) {
        if (config.getRoles().containsKey(roleName)) {
            throw new EntityNotFoundException("Role Limits with Role name=" + roleName + " already exists");
        }
    }

    public static void assertDefaultRole(String roleName, String restrictedAction) {
        if (CoreRole.DEFAULT_ROLE_NAME.equals(roleName)) {
            throw new ValidationException("'default' role can not be " + restrictedAction);
        }
    }

    public static void assertAssistantExists(Config config, String key) {
        if (!config.getAssistant().getAssistants().containsKey(key)) {
            throw new EntityNotFoundException("Can't find Assistant with name=" + key);
        }
    }

    public static void assertAssistantNotExists(Config config, String key) {
        if (config.getAssistant().getAssistants().containsKey(key)) {
            throw new EntityAlreadyExistsException("Assistant with name already exists=" + key);
        }
    }

    public static void assertModelExists(Config config, String modelName) {
        if (!config.getModels().containsKey(modelName)) {
            throw new EntityNotFoundException("Can't find Model with name=" + modelName);
        }
    }

    public static void assertRoleBasedEntityExists(Config config, String entityName) {
        boolean modelExists = Stream.of(config.getModels(), config.getApplications(), config.getAddons(), config.getAssistant().getAssistants())
                .map(Map::keySet)
                .anyMatch(names -> names.contains(entityName));
        if (!modelExists) {
            throw new EntityNotFoundException("Can't find entity with name=" + entityName);
        }
    }

    public static void assertModelNotExists(Config config, String modelName) {
        if (config.getModels().containsKey(modelName)) {
            throw new EntityAlreadyExistsException("Model with name already exists=" + modelName);
        }
    }

    public static void assertInterceptorExists(Config config, String interceptorName) {
        if (!config.getInterceptors().containsKey(interceptorName)) {
            throw new EntityNotFoundException("Can't find Interceptor with name=" + interceptorName);
        }
    }

    public static void assertInterceptorNotExists(Config config, String interceptorName) {
        if (config.getInterceptors().containsKey(interceptorName)) {
            throw new EntityAlreadyExistsException("Interceptor with name already exists=" + interceptorName);
        }
    }

    public static void assertAddonExists(Config config, String addonName) {
        if (!config.getAddons().containsKey(addonName)) {
            throw new EntityNotFoundException("Can't find Addon with name=" + addonName);
        }
    }

    public static void assertAddonNotExists(Config config, String addonName) {
        if (config.getAddons().containsKey(addonName)) {
            throw new EntityAlreadyExistsException("Addon with name already exists=" + addonName);
        }
    }

    public static void assertApplicationExists(Config config, String appName) {
        if (!config.getApplications().containsKey(appName)) {
            throw new EntityNotFoundException("Can't find Application with name=" + appName);
        }
    }

    public static void assertApplicationNotExists(Config config, String appName) {
        if (config.getApplications().containsKey(appName)) {
            throw new EntityNotFoundException("Application with name already exists=" + appName);
        }
    }

    public static void assertApplicationTypeSchemaExists(Config config, String id) {
        if (!config.getApplicationTypeSchemas().containsKey(id)) {
            throw new EntityNotFoundException("Can't find applicationTypeSchema with id=" + id);
        }
    }

    public static void assertApplicationTypeSchemaNotExists(Config config, String id) {
        if (config.getApplicationTypeSchemas().containsKey(id)) {
            throw new EntityAlreadyExistsException("ApplicationTypeSchema with id already exists=" + id);
        }
    }

    public static void assertRouteExists(Config config, String routeName) {
        if (!config.getRoutes().containsKey(routeName)) {
            throw new EntityNotFoundException("Can't find Route with name=" + routeName);
        }
    }

    public static void assertRouteNotExists(Config config, String routeName) {
        if (config.getRoutes().containsKey(routeName)) {
            throw new EntityAlreadyExistsException("Route with name already exists=" + routeName);
        }
    }

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
