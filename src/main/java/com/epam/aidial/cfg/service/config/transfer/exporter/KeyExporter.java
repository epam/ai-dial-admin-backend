package com.epam.aidial.cfg.service.config.transfer.exporter;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ExportConfigComponentType;
import com.epam.aidial.cfg.domain.model.ExportKeyInfo;
import com.epam.aidial.cfg.domain.model.Key;
import com.epam.aidial.cfg.domain.service.KeyService;
import com.epam.aidial.cfg.model.ExportRequest;
import com.epam.aidial.cfg.model.FullExportRequest;
import com.epam.aidial.cfg.model.SelectedItemsExportRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.domain.model.ExportFormat.CORE;
import static com.epam.aidial.cfg.service.config.transfer.exporter.util.ExportUtils.filterComponentsByTypeAndCollectToMap;
import static com.epam.aidial.cfg.service.config.transfer.exporter.util.ExportUtils.toLinkedHashMap;

@Service
@LogExecution
@RequiredArgsConstructor
@Slf4j
public class KeyExporter {

    private final KeyService keyService;

    protected Map<String, Key> getKeys(ExportRequest request) {
        Function<Key, String> keyMapper = switch (request.getExportFormat()) {
            case CORE -> Key::getKey;
            case ADMIN -> Key::getName;
        };
        if (request instanceof FullExportRequest fullExportRequest) {
            return fullExportRequest.getComponentTypes().contains(ExportConfigComponentType.KEY)
                    ? getKeys(fullExportRequest).stream()
                    .collect(toLinkedHashMap(keyMapper))
                    : new HashMap<>();
        } else if (request instanceof SelectedItemsExportRequest selectedItemsExportRequest) {
            return getKeys(selectedItemsExportRequest).stream()
                    .collect(toLinkedHashMap(keyMapper));
        }
        throw new IllegalArgumentException("Unsupported request type: " + request.getClass());
    }

    private Collection<Key> getKeys(FullExportRequest fullExportRequest) {
        var keys = fullExportRequest.getExportFormat() == CORE
                ? keyService.getAllValidKeysOrderedByDisplayNameAscNameAsc()
                : keyService.getAllKeysOrderedByDisplayNameAscNameAsc();
        return keys.stream()
                .map(key -> removeKey(key, fullExportRequest))
                .filter(Objects::nonNull)
                .map(key -> removeDependency(key, fullExportRequest.getComponentTypes()))
                .toList();
    }

    private List<Key> getKeys(SelectedItemsExportRequest selectedItemsExportRequest) {
        var componentsByName = filterComponentsByTypeAndCollectToMap(selectedItemsExportRequest.getComponents(), ExportConfigComponentType.KEY);
        if (componentsByName.isEmpty()) {
            return List.of();
        }
        return keyService.getAllByNamesOrderedByDisplayNameAscNameAsc(componentsByName.keySet()).stream()
                .filter(key -> isValidKey(key, selectedItemsExportRequest))
                .map(key -> removeKey(key, selectedItemsExportRequest))
                .filter(Objects::nonNull)
                .toList();
    }

    protected Collection<ExportKeyInfo> preview(ExportRequest request) {
        return getKeys(request).values().stream()
                .map(component -> ExportKeyInfo.builder()
                        .roles(component.getRoles())
                        .expiresAt(component.getExpiresAt())
                        .keyGeneratedAt(component.getKeyGeneratedAt())
                        .name(component.getName())
                        .displayName(component.getDisplayName())
                        .description(component.getDescription())
                        .type(ExportConfigComponentType.KEY)
                        .build())
                .collect(Collectors.toList());
    }

    private Key removeDependency(Key key, Set<ExportConfigComponentType> componentTypes) {
        if (!componentTypes.contains(ExportConfigComponentType.ROLE)) {
            key.setRoles(null);
        }
        return key;
    }

    private Key removeKey(Key key, ExportRequest exportRequest) {
        return switch (exportRequest.getExportFormat()) {
            case CORE -> removeCoreKey(key, exportRequest);
            case ADMIN -> removeAdminKey(key, exportRequest);
        };
    }

    private Key removeCoreKey(Key key, ExportRequest exportRequest) {
        boolean isBlankKeyValue = StringUtils.isBlank(key.getKey());
        if (!exportRequest.isAddSecrets() || isBlankKeyValue) {
            if (isBlankKeyValue) {
                log.debug("Remove invalid key with blank key value, key name: {}", key.getName());
            }
            return null;
        }

        return key;
    }

    private Key removeAdminKey(Key key, ExportRequest exportRequest) {
        if (!exportRequest.isAddSecrets()) {
            key.setKey(null);
        }
        return key;
    }

    private boolean isValidKey(Key key, SelectedItemsExportRequest selectedItemsExportRequest) {
        return selectedItemsExportRequest.getExportFormat() != CORE || key.getValidityState().isValid();
    }
}