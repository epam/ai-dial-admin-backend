package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.page.PageRequestModel;
import com.epam.aidial.cfg.dto.page.PageRequestDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PageDtoMapper {

    PageRequestModel toPageRequestModel(PageRequestDto dto);
}