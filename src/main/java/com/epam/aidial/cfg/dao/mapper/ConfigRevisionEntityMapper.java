package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.audit.model.ConfigRevisionEntity;
import com.epam.aidial.cfg.domain.model.ConfigRevision;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConfigRevisionEntityMapper {

    ConfigRevision map(ConfigRevisionEntity configRevisionEntity);

    @Mapping(target = "activities", ignore = true)
    ConfigRevisionEntity map(ConfigRevision configRevision);

}
