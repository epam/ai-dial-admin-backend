package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.source.AdapterSource;
import com.epam.aidial.cfg.domain.model.source.ModelContainerSource;
import com.epam.aidial.cfg.domain.model.source.ModelEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.ModelSource;
import com.epam.aidial.cfg.dto.source.AdapterSourceDto;
import com.epam.aidial.cfg.dto.source.ModelContainerSourceDto;
import com.epam.aidial.cfg.dto.source.ModelEndpointsSourceDto;
import com.epam.aidial.cfg.dto.source.ModelSourceDto;
import org.mapstruct.Mapper;
import org.mapstruct.SubclassExhaustiveStrategy;
import org.mapstruct.SubclassMapping;

@Mapper(componentModel = "spring", subclassExhaustiveStrategy = SubclassExhaustiveStrategy.RUNTIME_EXCEPTION)
public interface ModelSourceDtoMapper {

    @SubclassMapping(source = ModelEndpointsSourceDto.class, target = ModelEndpointsSource.class)
    @SubclassMapping(source = AdapterSourceDto.class, target = AdapterSource.class)
    @SubclassMapping(source = ModelContainerSourceDto.class, target = ModelContainerSource.class)
    ModelSource toModelSource(ModelSourceDto modelSourceDto);

    ModelEndpointsSource toModelEndpointsSource(ModelEndpointsSourceDto modelEndpointsSourceDto);

    AdapterSource toAdapterSource(AdapterSourceDto adapterSourceDto);

    ModelContainerSource toModelContainerSource(ModelContainerSourceDto modelContainerSourceDto);

    @SubclassMapping(source = ModelEndpointsSource.class, target = ModelEndpointsSourceDto.class)
    @SubclassMapping(source = AdapterSource.class, target = AdapterSourceDto.class)
    @SubclassMapping(source = ModelContainerSource.class, target = ModelContainerSourceDto.class)
    ModelSourceDto toModelSourceDto(ModelSource modelSource);

    ModelEndpointsSourceDto toModelEndpointsSourceDto(ModelEndpointsSource modelEndpointsSource);

    AdapterSourceDto toAdapterSourceDto(AdapterSource modelRunnerSource);

    ModelContainerSourceDto toModelContainerSourceDto(ModelContainerSource modelContainerSource);

}
