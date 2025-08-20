package com.epam.aidial.cfg.web.facade;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.service.ModelService;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.dto.ShareResourceLimitDto;
import com.epam.aidial.cfg.web.facade.mapper.ModelDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@LogExecution
public class ModelFacade {

    private final ModelService modelService;
    private final ModelDtoMapper mapper;

    public Collection<ModelDto> getAll() {
        return modelService.getAll()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public ModelDto getModel(String modelName) {
        Model model = modelService.getModel(modelName);
        return mapper.toDto(model);
    }

    public void createModel(ModelDto modelDto) {
        setDefaultRoleShareResourceLimitIfMissing(modelDto);
        Optional.of(modelDto)
                .map(mapper::toDomain)
                .ifPresent(modelService::createModel);
    }

    public void updateModel(String modelName, ModelDto modelDto) {
        setDefaultRoleShareResourceLimitIfMissing(modelDto);
        Model value = mapper.toDomain(modelDto);
        modelService.updateModel(modelName, value);
    }

    public void deleteModel(String model) {
        modelService.deleteModel(model);
    }

    public ModelDto getSnapshot(String modelName, Integer revision) {
        Model model = modelService.getSnapshot(modelName, revision);
        return mapper.toDto(model);
    }

    public Collection<ModelDto> getAllAtRevision(Integer revision) {
        return modelService.getAllAtRevision(revision)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    private void setDefaultRoleShareResourceLimitIfMissing(ModelDto modelDto) {
        ShareResourceLimitDto defaultRoleShareResourceLimit = modelDto.getDefaultRoleShareResourceLimit();
        if (defaultRoleShareResourceLimit == null) {
            defaultRoleShareResourceLimit = new ShareResourceLimitDto();
            modelDto.setDefaultRoleShareResourceLimit(defaultRoleShareResourceLimit);
        }
    }
}
