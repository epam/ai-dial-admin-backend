package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.source.ApplicationContainerSource;
import com.epam.aidial.cfg.domain.model.source.ApplicationEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.ApplicationSchemaSource;
import com.epam.aidial.cfg.domain.model.source.ApplicationSource;
import com.epam.aidial.cfg.dto.source.ApplicationContainerSourceDto;
import com.epam.aidial.cfg.dto.source.ApplicationEndpointsSourceDto;
import com.epam.aidial.cfg.dto.source.ApplicationSchemaSourceDto;
import com.epam.aidial.cfg.dto.source.ApplicationSourceDto;
import org.mapstruct.Mapper;
import org.mapstruct.SubclassExhaustiveStrategy;
import org.mapstruct.SubclassMapping;

@Mapper(componentModel = "spring", subclassExhaustiveStrategy = SubclassExhaustiveStrategy.RUNTIME_EXCEPTION)
public interface ApplicationSourceDtoMapper {

    @SubclassMapping(source = ApplicationEndpointsSourceDto.class, target = ApplicationEndpointsSource.class)
    @SubclassMapping(source = ApplicationSchemaSourceDto.class, target = ApplicationSchemaSource.class)
    @SubclassMapping(source = ApplicationContainerSourceDto.class, target = ApplicationContainerSource.class)
    ApplicationSource toApplicationSource(ApplicationSourceDto applicationSourceDto);

    ApplicationEndpointsSource toApplicationEndpointsSource(ApplicationEndpointsSourceDto applicationEndpointsSourceDto);

    ApplicationSchemaSource toApplicationSchemaSource(ApplicationSchemaSourceDto applicationSchemaSourceDto);

    ApplicationContainerSource toApplicationContainerSource(ApplicationContainerSourceDto applicationContainerSourceDto);

    @SubclassMapping(source = ApplicationEndpointsSource.class, target = ApplicationEndpointsSourceDto.class)
    @SubclassMapping(source = ApplicationSchemaSource.class, target = ApplicationSchemaSourceDto.class)
    @SubclassMapping(source = ApplicationContainerSource.class, target = ApplicationContainerSourceDto.class)
    ApplicationSourceDto toApplicationSourceDto(ApplicationSource applicationSource);

    ApplicationEndpointsSourceDto toApplicationEndpointsSourceDto(ApplicationEndpointsSource applicationEndpointsSource);

    ApplicationSchemaSourceDto toApplicationSchemaSourceDto(ApplicationSchemaSource applicationSchemaSource);

    ApplicationContainerSourceDto toApplicationContainerSourceDto(ApplicationContainerSource applicationContainerSource);

}
