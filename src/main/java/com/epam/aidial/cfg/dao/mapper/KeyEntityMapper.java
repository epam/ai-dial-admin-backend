package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.jpa.RoleJpaRepository;
import com.epam.aidial.cfg.dao.model.KeyEntity;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.domain.model.Key;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.google.api.client.util.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = RoleEntityMapper.class)
public abstract class KeyEntityMapper {

    @Autowired
    protected RoleJpaRepository roleJpaRepository;

    public abstract Key toDomain(KeyEntity entity);

    protected String mapRoleToString(RoleEntity value) {
        return value != null ? value.getName() : null;
    }

    public KeyEntity toEntity(Key domain, long keyGeneratedAt, KeyEntity entity) {
        List<RoleEntity> roleEntities = findRolesByNames(domain.getRoles());

        KeyEntity updatedEntity = update(domain, keyGeneratedAt, entity);

        updatedEntity.getRoles().forEach(role -> role.getKeys().remove(updatedEntity));
        roleEntities.forEach(role -> role.getKeys().add(updatedEntity));
        updatedEntity.getRoles().clear();
        updatedEntity.getRoles().addAll(roleEntities);

        return updatedEntity;
    }

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "keyGeneratedAt", source = "keyGeneratedAt")
    protected abstract KeyEntity update(Key domain, long keyGeneratedAt, @MappingTarget KeyEntity keyEntity);

    private List<RoleEntity> findRolesByNames(List<String> names) {
        if (CollectionUtils.isEmpty(names)) {
            return List.of();
        }

        List<RoleEntity> existingRoles = Lists.newArrayList(roleJpaRepository.findAllById(names));
        Set<String> existingRoleNames = existingRoles.stream().map(RoleEntity::getName).collect(Collectors.toSet());

        Set<String> namesDiff = SetUtils.difference(new HashSet<>(names), existingRoleNames);
        if (!namesDiff.isEmpty()) {
            throw new EntityNotFoundException("unable to find roles: " + namesDiff);
        }

        return existingRoles;
    }
}
