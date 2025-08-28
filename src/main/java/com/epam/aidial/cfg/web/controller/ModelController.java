package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.web.facade.ModelFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@Validated
@LogExecution
@RestController
@RequestMapping("/api/v1/models")
@RequiredArgsConstructor
public class ModelController {

    private final ModelFacade modelFacade;

    @GetMapping(produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<ModelDto> getAllModels() { //TODO change to model info
        return modelFacade.getAll();
    }

    @GetMapping(path = "/{modelName}",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelDto getModel(@PathVariable("modelName") String modelName) {
        return modelFacade.getModel(modelName);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createModel(@RequestBody @Valid ModelDto modelDto) {
        modelFacade.createModel(modelDto);
    }

    @PutMapping(path = "/{modelName}",
                consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateModel(@PathVariable("modelName") String modelName,
                            @RequestBody @Valid ModelDto modelDto) {
        modelFacade.updateModel(modelName, modelDto);
    }

    @DeleteMapping(path = "/{modelName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteModel(@PathVariable("modelName") String modelName) {
        modelFacade.deleteModel(modelName);
    }

    @GetMapping(path = "/{modelName}/revision/{revision}",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelDto getSnapshot(@PathVariable String modelName, @PathVariable Integer revision) {
        return modelFacade.getSnapshot(modelName, revision);
    }

    @GetMapping(path = "/revision/{revision}", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<ModelDto> getAllAtRevision(@PathVariable Integer revision) {
        return modelFacade.getAllAtRevision(revision);
    }
}
