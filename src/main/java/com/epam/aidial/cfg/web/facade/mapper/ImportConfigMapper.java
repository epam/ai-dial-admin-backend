package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.model.Addon;
import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import com.epam.aidial.cfg.domain.model.Assistant;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.model.ImportConfigPreview;
import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.domain.model.Key;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.model.Route;
import com.epam.aidial.cfg.dto.AdapterDto;
import com.epam.aidial.cfg.dto.AddonDto;
import com.epam.aidial.cfg.dto.ApplicationDto;
import com.epam.aidial.cfg.dto.ApplicationTypeSchemaDto;
import com.epam.aidial.cfg.dto.AssistantDto;
import com.epam.aidial.cfg.dto.ImportConfigPreviewDto;
import com.epam.aidial.cfg.dto.InterceptorDto;
import com.epam.aidial.cfg.dto.KeyDto;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.dto.RouteDto;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

@Mapper(componentModel = "spring")
public abstract class ImportConfigMapper {

    @Autowired
    private KeyDtoMapper keyDtoMapper;
    @Autowired
    private RoleDtoMapper roleDtoMapper;
    @Autowired
    private InterceptorDtoMapper interceptorDtoMapper;
    @Autowired
    private ApplicationTypeSchemaDtoMapper applicationTypeSchemaDtoMapper;
    @Autowired
    private ModelDtoMapper modelDtoMapper;
    @Autowired
    private ApplicationDtoMapper applicationDtoMapper;
    @Autowired
    private AssistantDtoMapper assistantDtoMapper;
    @Autowired
    private AddonDtoMapper addonDtoMapper;
    @Autowired
    private RouteDtoMapper routeDtoMapper;
    @Autowired
    private AdapterDtoMapper adapterDtoMapper;

    public abstract ImportConfigPreviewDto toImportConfigPreviewDto(ImportConfigPreview importConfigPreview);

    public Collection<ImportComponent<KeyDto>> mapKeys(Collection<ImportComponent<Key>> keys) {
        return mapGeneric(keys, keyDtoMapper::toDto);
    }

    public Collection<ImportComponent<RoleDto>> mapRoles(Collection<ImportComponent<Role>> roles) {
        return mapGeneric(roles, roleDtoMapper::toDto);
    }

    public Collection<ImportComponent<InterceptorDto>> mapInterceptors(Collection<ImportComponent<Interceptor>> interceptors) {
        return mapGeneric(interceptors, interceptorDtoMapper::toDto);
    }

    public Collection<ImportComponent<ApplicationTypeSchemaDto>> mapAppRunners(Collection<ImportComponent<ApplicationTypeSchema>> schemas) {
        return mapGeneric(schemas, applicationTypeSchemaDtoMapper::toDto);
    }

    public Collection<ImportComponent<RouteDto>> mapRoutes(Collection<ImportComponent<Route>> routes) {
        return mapGeneric(routes, routeDtoMapper::toDto);
    }

    public Collection<ImportComponent<ModelDto>> mapModels(Collection<ImportComponent<Model>> models) {
        return mapGeneric(models, modelDtoMapper::toDto);
    }

    public Collection<ImportComponent<ApplicationDto>> mapApplications(Collection<ImportComponent<Application>> applications) {
        return mapGeneric(applications, applicationDtoMapper::toDto);
    }

    public Collection<ImportComponent<AddonDto>> mapAddons(Collection<ImportComponent<Addon>> addons) {
        return mapGeneric(addons, addonDtoMapper::toDto);
    }

    public Collection<ImportComponent<AssistantDto>> mapAssistants(Collection<ImportComponent<Assistant>> assistants) {
        return mapGeneric(assistants, assistantDtoMapper::toDto);
    }

    public Collection<ImportComponent<AdapterDto>> mapAdapters(Collection<ImportComponent<Adapter>> adapters) {
        return mapGeneric(adapters, adapterDtoMapper::toDto);
    }

    public <T, D> Collection<ImportComponent<D>> mapGeneric(Collection<ImportComponent<T>> input,
                                                            Function<T, D> dtoMapper) {
        if (CollectionUtils.isEmpty(input)) {
            return Collections.emptyList();
        }

        return input.stream()
                .map(component -> new ImportComponent<>(component.getImportAction(), dtoMapper.apply(component.getValue())))
                .toList();
    }
}
