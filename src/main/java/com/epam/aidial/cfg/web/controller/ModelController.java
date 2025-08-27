package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.dto.OnUpdate;
import com.epam.aidial.cfg.web.facade.ModelFacade;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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
    public Collection<ModelDto> getAllModels(HttpServletResponse response) throws Exception { //TODO change to model info
        Collection<ModelDto> allModels = modelFacade.getAll();
        return allModels;
    }

    @GetMapping(path = "/{modelName}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelDto getModel(HttpServletResponse response,
                             @PathVariable("modelName") String modelName) throws Exception {
        var result = modelFacade.getModel(modelName);
        return result;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createModel(HttpServletResponse response,
                            @RequestBody @Valid ModelDto modelDto) throws Exception {
        modelFacade.createModel(modelDto);
    }

    @PutMapping(path = "/{modelName}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateModel(HttpServletResponse response,
                            @PathVariable("modelName") String modelName,
                            @RequestBody @Validated(OnUpdate.class) ModelDto modelDto) throws Exception {
        modelFacade.updateModel(modelName, modelDto);
    }

    @DeleteMapping(path = "/{modelName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteModel(HttpServletResponse response,
                            @PathVariable("modelName") String modelName) throws Exception {
        modelFacade.deleteModel(modelName);
    }

    @GetMapping(path = "/{modelName}/revision/{revision}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelDto getSnapshot(@PathVariable String modelName, @PathVariable Integer revision) {
        return modelFacade.getSnapshot(modelName, revision);
    }

    @GetMapping(path = "/revision/{revision}", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<ModelDto> getAllAtRevision(HttpServletResponse response, @PathVariable Integer revision) throws Exception {
        return modelFacade.getAllAtRevision(revision);
    }
}
