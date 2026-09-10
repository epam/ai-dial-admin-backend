package com.epam.aidial.cfg.mapper;

import com.epam.aidial.cfg.dto.ApplicationResourceDto;
import com.epam.aidial.cfg.dto.ApplicationResourceNodeInfoDto;
import com.epam.aidial.cfg.dto.ApplicationsEximDto;
import com.epam.aidial.cfg.dto.CreateApplicationResourceDto;
import com.epam.aidial.cfg.dto.source.ApplicationResourceEndpointsSourceDto;
import com.epam.aidial.cfg.dto.source.ApplicationResourceSchemaSourceDto;
import com.epam.aidial.cfg.dto.source.ApplicationResourceSourceDto;
import com.epam.aidial.cfg.model.ApplicationResource;
import com.epam.aidial.cfg.model.ApplicationResourceNodeInfo;
import com.epam.aidial.cfg.model.ApplicationsExim;
import com.epam.aidial.cfg.model.CreateApplicationResource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ApplicationResourceMapper {

    @Mapping(target = "applicationTypeSchemaId", source = "source", qualifiedByName = "toSchemaIdString")
    CreateApplicationResource toCreateApplicationResourceDto(CreateApplicationResourceDto createApplicationResourceDto);

    ApplicationResourceNodeInfoDto toApplicationResourceNodeInfoDto(ApplicationResourceNodeInfo applicationResourceNodeInfo);

    @Mapping(target = "source", source = "applicationTypeSchemaId", qualifiedByName = "toSourceDto")
    ApplicationResourceDto toApplicationResourceDto(ApplicationResource model);

    ApplicationsEximDto toApplicationsEximDto(ApplicationsExim model);

    @Named("toSourceDto")
    default ApplicationResourceSourceDto toSourceDto(String applicationTypeSchemaId) {
        if (applicationTypeSchemaId == null) {
            return new ApplicationResourceEndpointsSourceDto();
        }
        return new ApplicationResourceSchemaSourceDto(applicationTypeSchemaId);
    }

    @Named("toSchemaIdString")
    default String toSchemaIdString(ApplicationResourceSourceDto source) {
        if (source instanceof ApplicationResourceSchemaSourceDto schema) {
            return schema.applicationTypeSchemaId();
        }
        return null;
    }
}
