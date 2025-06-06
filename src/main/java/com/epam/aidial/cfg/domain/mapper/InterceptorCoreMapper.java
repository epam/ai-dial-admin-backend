package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.core.config.CoreInterceptor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InterceptorCoreMapper {

    @Mapping(target = "userRoles", ignore = true)
    @Mapping(target = "displayVersion", ignore = true)
    @Mapping(target = "reference", ignore = true)
    @Mapping(target = "features", ignore = true)
    @Mapping(target = "inputAttachmentTypes", ignore = true)
    @Mapping(target = "maxInputAttachments", ignore = true)
    @Mapping(target = "defaults", ignore = true)
    @Mapping(target = "interceptors", ignore = true)
    @Mapping(target = "descriptionKeywords", ignore = true)
    @Mapping(target = "maxRetryAttempts", ignore = true)
    CoreInterceptor mapInterceptor(Interceptor interceptor);

    @Mapping(target = "entities", ignore = true)
    Interceptor mapInterceptor(CoreInterceptor interceptor);

}
