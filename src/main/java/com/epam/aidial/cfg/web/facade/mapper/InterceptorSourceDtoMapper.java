package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.source.InterceptorContainerSource;
import com.epam.aidial.cfg.domain.model.source.InterceptorEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.InterceptorRunnerSource;
import com.epam.aidial.cfg.domain.model.source.InterceptorSource;
import com.epam.aidial.cfg.dto.source.InterceptorContainerSourceDto;
import com.epam.aidial.cfg.dto.source.InterceptorEndpointsSourceDto;
import com.epam.aidial.cfg.dto.source.InterceptorRunnerSourceDto;
import com.epam.aidial.cfg.dto.source.InterceptorSourceDto;
import org.mapstruct.Mapper;
import org.mapstruct.SubclassExhaustiveStrategy;
import org.mapstruct.SubclassMapping;

@Mapper(componentModel = "spring", subclassExhaustiveStrategy = SubclassExhaustiveStrategy.RUNTIME_EXCEPTION)
public interface InterceptorSourceDtoMapper {

    @SubclassMapping(source = InterceptorEndpointsSourceDto.class, target = InterceptorEndpointsSource.class)
    @SubclassMapping(source = InterceptorRunnerSourceDto.class, target = InterceptorRunnerSource.class)
    @SubclassMapping(source = InterceptorContainerSourceDto.class, target = InterceptorContainerSource.class)
    InterceptorSource toInterceptorSource(InterceptorSourceDto interceptorSourceDto);

    InterceptorEndpointsSource toInterceptorEndpointsSource(InterceptorEndpointsSourceDto interceptorEndpointsSourceDto);

    InterceptorRunnerSource toInterceptorRunnerSource(InterceptorRunnerSourceDto interceptorRunnerSourceDto);

    InterceptorContainerSource toInterceptorContainerSource(InterceptorContainerSourceDto interceptorContainerSourceDto);

    @SubclassMapping(source = InterceptorEndpointsSource.class, target = InterceptorEndpointsSourceDto.class)
    @SubclassMapping(source = InterceptorRunnerSource.class, target = InterceptorRunnerSourceDto.class)
    @SubclassMapping(source = InterceptorContainerSource.class, target = InterceptorContainerSourceDto.class)
    InterceptorSourceDto toInterceptorSourceDto(InterceptorSource interceptorSource);

    InterceptorEndpointsSourceDto toInterceptorEndpointsSourceDto(InterceptorEndpointsSource interceptorEndpointsSource);

    InterceptorRunnerSourceDto toInterceptorRunnerSourceDto(InterceptorRunnerSource interceptorRunnerSource);

    InterceptorContainerSourceDto toInterceptorContainerSourceDto(InterceptorContainerSource interceptorContainerSource);

}
