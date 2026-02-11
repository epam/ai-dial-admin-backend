package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.source.ModelAdapterSource;
import com.epam.aidial.cfg.domain.model.source.ModelContainerSource;
import com.epam.aidial.cfg.domain.model.source.ModelEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.ModelSource;
import com.epam.aidial.cfg.dto.source.ModelAdapterSourceDto;
import com.epam.aidial.cfg.dto.source.ModelContainerSourceDto;
import com.epam.aidial.cfg.dto.source.ModelEndpointsSourceDto;
import com.epam.aidial.cfg.dto.source.ModelSourceDto;
import org.mapstruct.Mapper;
import org.mapstruct.SubclassExhaustiveStrategy;
import org.mapstruct.SubclassMapping;

@Mapper(componentModel = "spring", subclassExhaustiveStrategy = SubclassExhaustiveStrategy.RUNTIME_EXCEPTION)
public interface ModelSourceDtoMapper {

    @SubclassMapping(source = ModelEndpointsSourceDto.class, target = ModelEndpointsSource.class)
    @SubclassMapping(source = ModelAdapterSourceDto.class, target = ModelAdapterSource.class)
    @SubclassMapping(source = ModelContainerSourceDto.class, target = ModelContainerSource.class)
    ModelSource toModelSource(ModelSourceDto modelSourceDto);

    ModelEndpointsSource toModelEndpointsSource(ModelEndpointsSourceDto modelEndpointsSourceDto);

    ModelAdapterSource toModelAdapterSource(ModelAdapterSourceDto modelAdapterSourceDto);

    ModelContainerSource toModelContainerSource(ModelContainerSourceDto modelContainerSourceDto);

    @SubclassMapping(source = ModelEndpointsSource.class, target = ModelEndpointsSourceDto.class)
    @SubclassMapping(source = ModelAdapterSource.class, target = ModelAdapterSourceDto.class)
    @SubclassMapping(source = ModelContainerSource.class, target = ModelContainerSourceDto.class)
    ModelSourceDto toModelSourceDto(ModelSource modelSource);

    ModelEndpointsSourceDto toModelEndpointsSourceDto(ModelEndpointsSource modelEndpointsSource);

    ModelAdapterSourceDto toModelAdapterSourceDto(ModelAdapterSource modelAdapterSource);

    ModelContainerSourceDto toModelContainerSourceDto(ModelContainerSource modelContainerSource);

}
