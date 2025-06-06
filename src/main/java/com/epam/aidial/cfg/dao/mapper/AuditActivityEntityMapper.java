package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.audit.model.AuditActivityEntity;
import com.epam.aidial.cfg.domain.model.AuditActivity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditActivityEntityMapper {

    AuditActivity map(AuditActivityEntity entity);

}
