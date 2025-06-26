package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.ApplicationDto;
import com.epam.aidial.cfg.dto.AssistantDto;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.web.facade.AssistantFacade;
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
@RequestMapping("/api/v1/assistants")
@Validated
@LogExecution
public class AssistantController {

    private final AssistantFacade assistantFacade;

    public AssistantController(AssistantFacade assistantFacade) {
        this.assistantFacade = assistantFacade;
    }

    @GetMapping(produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<AssistantDto> getAll(HttpServletResponse response) throws Exception {
        return assistantFacade.getAllAssistants();
    }

    @GetMapping(path = "/{assistantName}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public AssistantDto getAssistant(HttpServletResponse response,
                                     @PathVariable("assistantName") String assistantName) throws Exception {
        return assistantFacade.getAssistant(assistantName);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createAssistant(HttpServletResponse response,
                            @RequestBody @Valid AssistantDto assistantDto) throws Exception {
        assistantFacade.createAssistant(assistantDto);
    }

    @DeleteMapping(path = "/{assistantName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAssistant(HttpServletResponse response, @PathVariable("assistantName") String assistantName) throws Exception {
        assistantFacade.deleteAssistant(assistantName);
    }

    @PutMapping(path = "/{assistantName}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateAssistant(HttpServletResponse response,
                           @PathVariable("assistantName") String assistantName,
                           @RequestBody @Valid AssistantDto assistantDto)
            throws Exception {
        assistantFacade.updateAssistant(assistantName, assistantDto);
    }

    @GetMapping(path = "/{assistantName}/revision/{revision}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public AssistantDto getSnapshot(@PathVariable String assistantName, @PathVariable Integer revision) {
        return assistantFacade.getSnapshot(assistantName, revision);
    }

    @GetMapping(path = "/revision/{revision}", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<AssistantDto> getAllAtRevision(HttpServletResponse response, @PathVariable Integer revision) throws Exception {
        return assistantFacade.getAllAtRevision(revision);
    }
}
