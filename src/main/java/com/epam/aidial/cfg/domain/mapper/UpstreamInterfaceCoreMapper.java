package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.UpstreamInterface;
import com.epam.aidial.core.config.CoreUpstreamInterface;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UpstreamInterfaceCoreMapper {

    CoreUpstreamInterface map(UpstreamInterface upstreamInterface);

    UpstreamInterface map(CoreUpstreamInterface coreUpstreamInterface);
}
