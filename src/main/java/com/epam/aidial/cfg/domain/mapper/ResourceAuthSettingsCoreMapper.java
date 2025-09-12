package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.ResourceAuthSettings;
import com.epam.aidial.core.config.CoreResourceAuthSettings;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ResourceAuthSettingsCoreMapper {

    CoreResourceAuthSettings toCoreResourceAuthSettings(ResourceAuthSettings domain);

    ResourceAuthSettings toResourceAuthSettings(CoreResourceAuthSettings coreModel);
}
