package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.GlobalInterceptorEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.IntStream;

@Mapper(componentModel = "spring", uses = {
        InterceptorContainerEntityMapper.class, FeaturesEntityMapper.class, MapPropertiesMapper.class
})
public abstract class GlobalSettingsMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "interceptorOrder", source = "order")
    public abstract GlobalInterceptorEntity toGlobalInterceptorEntity(String name, Integer order);

    public List<GlobalInterceptorEntity> toGlobalInterceptorEntity(List<String> globalInterceptorIds) {
        return IntStream.range(0, globalInterceptorIds.size())
                .mapToObj(index -> toGlobalInterceptorEntity(globalInterceptorIds.get(index), index))
                .toList();
    }
}