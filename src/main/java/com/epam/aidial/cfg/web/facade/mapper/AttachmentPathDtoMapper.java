package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.AttachmentPath;
import com.epam.aidial.cfg.dto.AttachmentPathDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AttachmentPathDtoMapper {

    AttachmentPath toDomain(AttachmentPathDto entity);

    AttachmentPathDto toDto(AttachmentPath domain);
}
