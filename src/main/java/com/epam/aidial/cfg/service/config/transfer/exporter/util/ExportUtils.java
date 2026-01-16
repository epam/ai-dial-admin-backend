package com.epam.aidial.cfg.service.config.transfer.exporter.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Set;

@UtilityClass
public class ExportUtils {

    public static boolean hasAnyRequestedTopic(Set<String> entityTopics, Set<String> requestedTopics) {
        return CollectionUtils.isEmpty(requestedTopics)
                || (entityTopics != null && requestedTopics.stream().anyMatch(entityTopics::contains));
    }
}
