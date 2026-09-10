package com.epam.aidial.cfg.domain.util;

import com.epam.aidial.cfg.dao.model.AdapterContainerEntity;
import com.epam.aidial.cfg.dao.model.ApplicationContainerEntity;
import com.epam.aidial.cfg.dao.model.InterceptorContainerEntity;
import com.epam.aidial.cfg.dao.model.ModelContainerEntity;
import com.epam.aidial.cfg.dao.model.ToolSetContainerEntity;
import com.epam.aidial.cfg.domain.model.source.AdapterContainerSource;
import com.epam.aidial.cfg.domain.model.source.ApplicationContainerSource;
import com.epam.aidial.cfg.domain.model.source.InterceptorContainerSource;
import com.epam.aidial.cfg.domain.model.source.ModelContainerSource;
import com.epam.aidial.cfg.domain.model.source.ToolSetContainerSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Utility class for detecting changes in container source properties
 * between incoming domain models and existing entities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ContainerSourceChangeDetector {

    public static boolean hasSourceChanged(AdapterContainerSource incoming, AdapterContainerEntity existing) {
        return hasCommonFieldsChanged(incoming.getContainerId(), incoming.getCompletionEndpointPath(),
                existing.getContainerId(), existing.getCompletionEndpointPath())
                || !Objects.equals(incoming.getResponsesEndpointPath(), existing.getResponsesEndpointPath());
    }

    public static boolean hasSourceChanged(ApplicationContainerSource incoming, ApplicationContainerEntity existing) {
        return hasCommonFieldsChanged(incoming.getContainerId(), incoming.getCompletionEndpointPath(),
                existing.getContainerId(), existing.getCompletionEndpointPath())
                || !Objects.equals(incoming.getMcpEndpointPath(), existing.getMcpEndpointPath());
    }

    public static boolean hasSourceChanged(ModelContainerSource incoming, ModelContainerEntity existing) {
        return hasCommonFieldsChanged(incoming.getContainerId(), incoming.getCompletionEndpointPath(),
                existing.getContainerId(), existing.getCompletionEndpointPath())
                || !Objects.equals(incoming.getResponsesEndpointPath(), existing.getResponsesEndpointPath());
    }

    public static boolean hasSourceChanged(InterceptorContainerSource incoming, InterceptorContainerEntity existing) {
        return hasCommonFieldsChanged(incoming.getContainerId(), incoming.getCompletionEndpointPath(),
                existing.getContainerId(), existing.getCompletionEndpointPath())
                || !Objects.equals(incoming.getConfigurationEndpointPath(), existing.getConfigurationEndpointPath());
    }

    public static boolean hasSourceChanged(ToolSetContainerSource incoming, ToolSetContainerEntity existing) {
        return hasCommonFieldsChanged(incoming.getContainerId(), incoming.getCompletionEndpointPath(),
                existing.getContainerId(), existing.getCompletionEndpointPath());
    }

    private static boolean hasCommonFieldsChanged(String incomingContainerId, String incomingCompletionPath,
                                                  String existingContainerId, String existingCompletionPath) {
        return !Objects.equals(incomingContainerId, existingContainerId)
                || !Objects.equals(incomingCompletionPath, existingCompletionPath);
    }
}
