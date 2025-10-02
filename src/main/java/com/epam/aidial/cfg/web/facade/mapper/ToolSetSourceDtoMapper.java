package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.source.ToolSetContainerSource;
import com.epam.aidial.cfg.domain.model.source.ToolSetEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.ToolSetSource;
import com.epam.aidial.cfg.dto.source.ToolSetContainerSourceDto;
import com.epam.aidial.cfg.dto.source.ToolSetEndpointsSourceDto;
import com.epam.aidial.cfg.dto.source.ToolSetSourceDto;
import org.mapstruct.Mapper;
import org.mapstruct.SubclassExhaustiveStrategy;
import org.mapstruct.SubclassMapping;

@Mapper(componentModel = "spring", subclassExhaustiveStrategy = SubclassExhaustiveStrategy.RUNTIME_EXCEPTION)
public interface ToolSetSourceDtoMapper {

    @SubclassMapping(source = ToolSetEndpointsSourceDto.class, target = ToolSetEndpointsSource.class)
    @SubclassMapping(source = ToolSetContainerSourceDto.class, target = ToolSetContainerSource.class)
    ToolSetSource toToolSetSource(ToolSetSourceDto toolSetSourceDto);

    ToolSetEndpointsSource toToolSetEndpointsSource(ToolSetEndpointsSourceDto toolSetEndpointsSourceDto);

    ToolSetContainerSource toToolSetContainerSource(ToolSetContainerSourceDto toolSetContainerSourceDto);

    @SubclassMapping(source = ToolSetEndpointsSource.class, target = ToolSetEndpointsSourceDto.class)
    @SubclassMapping(source = ToolSetContainerSource.class, target = ToolSetContainerSourceDto.class)
    ToolSetSourceDto toToolSetSourceDto(ToolSetSource toolSetSource);

    ToolSetEndpointsSourceDto toToolSetEndpointsSourceDto(ToolSetEndpointsSource toolSetEndpointsSource);

    ToolSetContainerSourceDto toToolSetContainerSourceDto(ToolSetContainerSource toolSetContainerSource);

}
