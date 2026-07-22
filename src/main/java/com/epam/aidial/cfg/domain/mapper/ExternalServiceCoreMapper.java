package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.ExternalService;
import com.epam.aidial.core.config.CoreExternalService;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = ResourceAuthSettingsCoreMapper.class)
public interface ExternalServiceCoreMapper {

    CoreExternalService map(ExternalService externalService);

    ExternalService map(CoreExternalService coreExternalService);
}
