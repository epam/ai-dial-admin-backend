package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.EntitySyncStateDto;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.web.facade.ModelFacade;
import com.epam.aidial.core.config.CoreModel;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@Validated
@LogExecution
@RestController
@RequestMapping("/api/v1/models")
@RequiredArgsConstructor
public class ModelController extends AbstractController {

    private final ModelFacade modelFacade;

    @GetMapping(produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<ModelDto> getAllModels() { //TODO change to model info
        return modelFacade.getAll();
    }

    @GetMapping(path = "/{modelName}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ModelDto> getModel(@PathVariable("modelName") String modelName,
                                             @RequestHeader(value = "If-None-Match") String previousHash) {
        var dtoWithHash = modelFacade.getModelWithHash(modelName);
        return responseEntityForGet(dtoWithHash.dto(), dtoWithHash.hash(), previousHash);
    }

    @GetMapping(path = "/core/{modelName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CoreModel> getCoreModel(@PathVariable String modelName,
                                                  @RequestHeader(value = "If-None-Match") String previousHash) {
        var coreWithHash = modelFacade.getCoreModelWithHash(modelName);
        return responseEntityForGet(coreWithHash.core(), coreWithHash.hash(), previousHash);
    }

    @GetMapping(path = "/{modelName}/sync-state", produces = MediaType.APPLICATION_JSON_VALUE)
    public EntitySyncStateDto getSyncState(@PathVariable String modelName) {
        return modelFacade.getSyncState(modelName);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createModel(@RequestBody @Valid ModelDto modelDto) {
        modelFacade.createModel(modelDto);
    }

    @PutMapping(path = "/{modelName}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateModel(@PathVariable("modelName") String modelName,
                                            @RequestBody @Valid ModelDto modelDto,
                                            @RequestHeader(value = "If-Match") String previousHash) {
        var newHash = modelFacade.updateModel(modelName, modelDto, StringUtils.unwrap(previousHash, '"'));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).eTag(newHash).build();
    }

    @PutMapping(path = "/core/{modelName}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateModel(@PathVariable String modelName,
                                            @RequestBody @Valid CoreModel coreModel,
                                            @RequestHeader(value = "If-Match") String previousHash) {
        var newHash = modelFacade.updateModel(modelName, coreModel, StringUtils.unwrap(previousHash, '"'));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).eTag(newHash).build();
    }

    @DeleteMapping(path = "/{modelName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteModel(@PathVariable("modelName") String modelName) {
        modelFacade.deleteModel(modelName);
    }

    @GetMapping(path = "/{modelName}/revision/{revision}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelDto getSnapshot(@PathVariable String modelName, @PathVariable Integer revision) {
        return modelFacade.getSnapshot(modelName, revision);
    }

    @GetMapping(path = "/revision/{revision}", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<ModelDto> getAllAtRevision(@PathVariable Integer revision) {
        return modelFacade.getAllAtRevision(revision);
    }
}
