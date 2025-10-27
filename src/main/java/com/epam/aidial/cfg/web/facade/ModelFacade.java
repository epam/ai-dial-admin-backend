package com.epam.aidial.cfg.web.facade;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.ModelCoreMapper;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.service.ModelService;
import com.epam.aidial.cfg.dto.DtoWithDomainHash;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.service.transfer.importer.ConfigImporter;
import com.epam.aidial.cfg.web.facade.mapper.ModelDtoMapper;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@LogExecution
public class ModelFacade {

    private final ModelService modelService;
    private final ModelDtoMapper mapper;
    private final ModelCoreMapper modelCoreMapper;
    private final ConfigImporter configImporter;

    public Collection<ModelDto> getAll() {
        return modelService.getAll()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public DtoWithDomainHash<ModelDto> getModelWithHash(String modelName) {
        var modelWithHash = modelService.getModelWithHash(modelName);
        ModelDto dto = mapper.toDto(modelWithHash.model());
        return new DtoWithDomainHash<>(dto, modelWithHash.hash());
    }

    public ModelDto getModel(String modelName) {
        Model model = modelService.getModel(modelName);
        return mapper.toDto(model);
    }

    public void createModel(ModelDto modelDto) {
        Optional.of(modelDto)
                .map(mapper::toDomain)
                .ifPresent(modelService::createModel);
    }

    public String updateModel(String modelName, ModelDto modelDto, String hash) {
        Model value = mapper.toDomain(modelDto);
        return modelService.updateModel(modelName, value, hash);
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

    public CoreModel getCoreModel(String modelName) {
        Model model = modelService.getModel(modelName);
        return modelCoreMapper.mapModel(model);
    }

    public void updateCoreModel(String modelName, CoreModel coreModel) {
        modelService.assertExists(modelName);

        Map<String, CoreModel> coreModels = new HashMap<>(1);
        coreModels.put(modelName, coreModel);

        Config config = new Config();
        config.setModels(coreModels);

        configImporter.importConfigWithOverride(config);
    }
}
