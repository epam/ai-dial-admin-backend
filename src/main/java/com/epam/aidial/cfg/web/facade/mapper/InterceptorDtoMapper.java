package com.epam.aidial.cfg.web.facade.mapper;

import java.time.Instant;

import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.dto.InterceptorDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface InterceptorDtoMapper {

    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "instantToLong")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "instantToLong")
    Interceptor toDomain(InterceptorDto entity);

    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "longToInstant")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "longToInstant")
    InterceptorDto toDto(Interceptor domain);

    @Named("instantToLong")
    static Long mapInstantToLong(Instant instant) {
        return instant != null ? instant.toEpochMilli() : null;
    }

    @Named("longToInstant")
    static Instant mapLongToInstant(Long epochMilli) {
        return epochMilli != null ? Instant.ofEpochMilli(epochMilli) : null;
    }
}
