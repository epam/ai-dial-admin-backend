package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.ToolSetDto;
import com.epam.aidial.cfg.web.facade.ToolSetFacade;
import io.modelcontextprotocol.spec.McpSchema;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@Validated
@LogExecution
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/toolSets")
public class ToolSetController {

    private final ToolSetFacade toolSetFacade;

    @GetMapping(produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<ToolSetDto> getAllToolSets() {
        return toolSetFacade.getAllToolSets();
    }

    @GetMapping(path = "/{toolSetName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ToolSetDto getToolSet(@PathVariable("toolSetName") String toolSetName) {
        return toolSetFacade.getToolSet(toolSetName);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createToolSet(@RequestBody @Valid ToolSetDto toolSetName) {
        toolSetFacade.createToolSet(toolSetName);
    }

    @PutMapping(path = "/{toolSetName}",
                consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateToolSet(@PathVariable("toolSetName") String toolSetName,
                              @RequestBody @Valid ToolSetDto toolSetDto) {
        toolSetFacade.updateToolSet(toolSetName, toolSetDto);
    }

    @DeleteMapping(path = "/{toolSetName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteToolSet(@PathVariable("toolSetName") String toolSetName) {
        toolSetFacade.deleteToolSet(toolSetName);
    }

    @GetMapping(path = "/{toolSetName}/revision/{revision}",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ToolSetDto getSnapshot(@PathVariable String toolSetName, @PathVariable Integer revision) {
        return toolSetFacade.getSnapshot(toolSetName, revision);
    }

    @GetMapping(path = "/revision/{revision}", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<ToolSetDto> getAllAtRevision(@PathVariable Integer revision) throws Exception {
        return toolSetFacade.getAllAtRevision(revision);
    }

    @GetMapping(path = "/{toolSetName}/discovered-tools", produces = MediaType.APPLICATION_JSON_VALUE)
    public McpSchema.ListToolsResult getDiscoveredTools(@PathVariable("toolSetName") String toolSetName,
                                              @RequestParam(required = false) String nextCursor) {
        return toolSetFacade.getDiscoveredTools(toolSetName, nextCursor);
    }
}
