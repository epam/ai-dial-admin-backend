package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Key;
import com.epam.aidial.core.config.CoreKey;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface KeyCoreMapper {

    @Mapping(target = "role", ignore = true)
    @Mapping(target = "key", ignore = true)
    CoreKey mapKeyWithoutKeyValue(Key key);

    @Mapping(target = "role", ignore = true)
    CoreKey mapKey(Key key);

    default Key mapKey(CoreKey coreKey, String name) {
        Key key = new Key();
        key.setName(name);
        key.setDisplayName(name);
        return mapKey(coreKey, key);
    }

    @Mapping(target = "projectContactPoint", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "expiresAt", ignore = true)
    @Mapping(target = "keyGeneratedAt", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "displayName", ignore = true)
    @Mapping(target = "name", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Key mapKey(CoreKey coreKey, @MappingTarget Key key);

    @AfterMapping
    default void addRole(@MappingTarget Key key, CoreKey coreKey) {
        if (StringUtils.isNotEmpty(coreKey.getRole())) {
            List<String> roles = CollectionUtils.isNotEmpty(key.getRoles()) ? key.getRoles() : new ArrayList<>();
            roles.add(coreKey.getRole());
            key.setRoles(roles);
        }
    }

}
