package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.KeyEntity;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.domain.model.Key;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {RoleEntityMapper.class, ValidityStateEntityMapper.class})
public abstract class KeyEntityMapper {

    public abstract Key toDomain(KeyEntity entity);

    protected String mapRoleToString(RoleEntity value) {
        return value != null ? value.getName() : null;
    }

    public KeyEntity toEntity(Key domain, long keyGeneratedAt, KeyEntity entity, List<RoleEntity> roleEntities) {
        KeyEntity updatedEntity = update(domain, keyGeneratedAt, entity);

        updatedEntity.getRoles().stream()
                .filter(role -> !roleEntities.contains(role))
                .forEach(role -> role.getKeys().remove(updatedEntity));
        roleEntities.stream()
                .filter(role -> !updatedEntity.getRoles().contains(role))
                .forEach(role -> role.getKeys().add(updatedEntity));
        updatedEntity.getRoles().clear();
        updatedEntity.getRoles().addAll(roleEntities);

        return updatedEntity;
    }

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "validityState", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "keyGeneratedAt", source = "keyGeneratedAt")
    protected abstract KeyEntity update(Key domain, long keyGeneratedAt, @MappingTarget KeyEntity keyEntity);
}
