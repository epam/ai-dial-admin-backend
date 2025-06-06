package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.utils.ModelEndpointUtils;
import com.epam.aidial.cfg.dto.ModelDto;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class ModelEndpointDtoMapper {

    @Autowired
    protected ModelEndpointUtils modelEndpointUtils;

    @Named("mapEndpointFromModel")
    public String mapModelToEndpoint(Model model) {
        return modelEndpointUtils.createEndpoint(model);
    }
}
