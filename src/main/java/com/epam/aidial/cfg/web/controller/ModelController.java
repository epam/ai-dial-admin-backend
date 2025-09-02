package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.web.facade.ModelFacade;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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

@RestController
@RequestMapping("/api/v1/models")
@Validated
@LogExecution
public class ModelController {

    private final ModelFacade modelFacade;

    public ModelController(ModelFacade modelFacade) {
        this.modelFacade = modelFacade;
    }


    @GetMapping(produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<ModelDto> getAllModels(HttpServletResponse response) { //TODO change to model info
        Collection<ModelDto> allModels = modelFacade.getAll();
        return allModels;
    }

    @GetMapping(path = "/{modelName}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ModelDto> getModel(HttpServletResponse response,
                                             @PathVariable("modelName") String modelName,
                                             @RequestHeader(value = "If-None-Match") String previousHash) {
        var dtoWithHash = modelFacade.getModelWithHash(modelName);
        return dtoWithHash.hash().equals(StringUtils.unwrap(previousHash, '"'))
                ? ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(dtoWithHash.hash()).build()
                : ResponseEntity.status(HttpStatus.OK).eTag(dtoWithHash.hash()).body(dtoWithHash.dto());
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createModel(HttpServletResponse response,
                            @RequestBody @Valid ModelDto modelDto) {
        modelFacade.createModel(modelDto);
    }

    @PutMapping(path = "/{modelName}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateModel(HttpServletResponse response,
                                            @PathVariable("modelName") String modelName,
                                            @RequestBody @Valid ModelDto modelDto,
                                            @RequestHeader(value = "If-Match") String previousHash) {
        var newHash = modelFacade.updateModel(modelName, modelDto, StringUtils.unwrap(previousHash, '"'));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).eTag(newHash).build();
    }

    @DeleteMapping(path = "/{modelName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteModel(HttpServletResponse response,
                            @PathVariable("modelName") String modelName) {
        modelFacade.deleteModel(modelName);
    }

    @GetMapping(path = "/{modelName}/revision/{revision}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelDto getSnapshot(@PathVariable String modelName, @PathVariable Integer revision) {
        return modelFacade.getSnapshot(modelName, revision);
    }

    @GetMapping(path = "/revision/{revision}", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<ModelDto> getAllAtRevision(HttpServletResponse response, @PathVariable Integer revision) {
        return modelFacade.getAllAtRevision(revision);
    }
}
