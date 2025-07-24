package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.InterceptorContainerEntity;
import com.epam.aidial.cfg.domain.model.source.InterceptorContainerSource;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InterceptorContainerEntityMapper {

    InterceptorContainerEntity toEntity(InterceptorContainerSource domain);

    InterceptorContainerSource toDomain(InterceptorContainerEntity entity);
}
