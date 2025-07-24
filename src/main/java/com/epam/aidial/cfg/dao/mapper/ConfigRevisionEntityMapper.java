package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.audit.model.ConfigRevisionEntity;
import com.epam.aidial.cfg.domain.model.ConfigRevision;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConfigRevisionEntityMapper {

    ConfigRevision map(ConfigRevisionEntity configRevisionEntity);

}
