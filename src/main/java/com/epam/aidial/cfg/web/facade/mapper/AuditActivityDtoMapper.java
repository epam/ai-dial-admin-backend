package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.AuditActivity;
import com.epam.aidial.cfg.dto.AuditActivityDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditActivityDtoMapper {

    AuditActivityDto map(AuditActivity configRevision);

}
