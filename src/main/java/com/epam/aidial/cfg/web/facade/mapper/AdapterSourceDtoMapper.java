package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.source.AdapterContainerSource;
import com.epam.aidial.cfg.domain.model.source.AdapterEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.AdapterSource;
import com.epam.aidial.cfg.dto.source.AdapterContainerSourceDto;
import com.epam.aidial.cfg.dto.source.AdapterEndpointsSourceDto;
import com.epam.aidial.cfg.dto.source.AdapterSourceDto;
import org.mapstruct.Mapper;
import org.mapstruct.SubclassExhaustiveStrategy;
import org.mapstruct.SubclassMapping;

@Mapper(componentModel = "spring", subclassExhaustiveStrategy = SubclassExhaustiveStrategy.RUNTIME_EXCEPTION)
public interface AdapterSourceDtoMapper {

    @SubclassMapping(source = AdapterEndpointsSourceDto.class, target = AdapterEndpointsSource.class)
    @SubclassMapping(source = AdapterContainerSourceDto.class, target = AdapterContainerSource.class)
    AdapterSource toAdapterSource(AdapterSourceDto adapterSourceDto);

    AdapterEndpointsSource toAdapterEndpointsSource(AdapterEndpointsSourceDto adapterEndpointsSourceDto);

    AdapterContainerSource toAdapterContainerSource(AdapterContainerSourceDto adapterContainerSourceDto);

    @SubclassMapping(source = AdapterEndpointsSource.class, target = AdapterEndpointsSourceDto.class)
    @SubclassMapping(source = AdapterContainerSource.class, target = AdapterContainerSourceDto.class)
    AdapterSourceDto toAdapterSourceDto(AdapterSource adapterSource);

    AdapterEndpointsSourceDto toAdapterEndpointsSourceDto(AdapterEndpointsSource adapterEndpointsSource);

    AdapterContainerSourceDto toAdapterContainerSourceDto(AdapterContainerSource adapterContainerSource);
}
