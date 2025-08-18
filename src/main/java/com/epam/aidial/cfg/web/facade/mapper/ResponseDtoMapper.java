package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.Response;
import com.epam.aidial.cfg.dto.ResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ResponseDtoMapper {

    Response toDomain(ResponseDto entity);

    ResponseDto toDto(Response domain);
}
