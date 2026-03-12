package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.EntitySyncStateDto;
import com.epam.aidial.cfg.web.security.FullAdminOnly;
import com.epam.aidial.cfg.dto.ResourceSignInRequestDto;
import com.epam.aidial.cfg.dto.ResourceSignOutRequestDto;
import com.epam.aidial.cfg.dto.ToolSetDto;
import com.epam.aidial.cfg.web.facade.ToolSetFacade;
import com.epam.aidial.core.config.CoreToolSet;
import io.modelcontextprotocol.spec.McpSchema;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@Validated
@LogExecution
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/toolSets")
public class ToolSetController extends AbstractController {

    private final ToolSetFacade toolSetFacade;

    @GetMapping(produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<ToolSetDto> getAllToolSets() {
        return toolSetFacade.getAllToolSets();
    }

    @GetMapping(path = "/{toolSetName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ToolSetDto> getToolSet(@PathVariable("toolSetName") String toolSetName,
                                                 @RequestHeader(value = "If-None-Match") String previousHash) {
        var dtoWithHash = toolSetFacade.getToolSetWithHash(toolSetName);
        return responseEntityForGet(dtoWithHash.dto(), dtoWithHash.hash(), previousHash);
    }

    @GetMapping(path = "/core/{toolSetName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CoreToolSet> getCoreToolSet(@PathVariable String toolSetName,
                                                      @RequestHeader(value = "If-None-Match") String previousHash) {
        var coreWithHash = toolSetFacade.getCoreToolSetWithHash(toolSetName);
        return responseEntityForGet(coreWithHash.core(), coreWithHash.hash(), previousHash);
    }

    @GetMapping(path = "/{toolSetName}/sync-state", produces = MediaType.APPLICATION_JSON_VALUE)
    public EntitySyncStateDto getSyncState(@PathVariable String toolSetName,
                                           @RequestHeader(value = "If-Match") String previousHash) {
        return toolSetFacade.getSyncState(toolSetName, StringUtils.unwrap(previousHash, '"'));
    }

    @FullAdminOnly
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createToolSet(@RequestBody @Valid ToolSetDto toolSetName) {
        toolSetFacade.createToolSet(toolSetName);
    }

    @FullAdminOnly
    @PutMapping(path = "/{toolSetName}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateToolSet(@PathVariable("toolSetName") String toolSetName,
                                              @RequestBody @Valid ToolSetDto toolSetDto,
                                              @RequestHeader(value = "If-Match") String previousHash) {
        var newHash = toolSetFacade.updateToolSet(toolSetName, toolSetDto, StringUtils.unwrap(previousHash, '"'));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).eTag(newHash).build();
    }

    @FullAdminOnly
    @PutMapping(path = "/core/{toolSetName}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateToolSet(@PathVariable String toolSetName,
                                              @RequestBody @Valid CoreToolSet coreToolSet,
                                              @RequestHeader(value = "If-Match") String previousHash) {
        String newHash = toolSetFacade.updateToolSet(toolSetName, coreToolSet, StringUtils.unwrap(previousHash, '"'));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).eTag(newHash).build();
    }

    @FullAdminOnly
    @DeleteMapping(path = "/{toolSetName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteToolSet(@PathVariable("toolSetName") String toolSetName) {
        toolSetFacade.deleteToolSet(toolSetName);
    }

    @GetMapping(path = "/{toolSetName}/revision/{revision}", produces = MediaType.APPLICATION_JSON_VALUE)
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

    @PostMapping(path = "/{toolSetName}/call-tool", produces = MediaType.APPLICATION_JSON_VALUE)
    public McpSchema.CallToolResult getDiscoveredTools(@PathVariable String toolSetName,
                                                       @RequestBody McpSchema.CallToolRequest callToolRequest) {
        return toolSetFacade.callTool(toolSetName, callToolRequest);
    }

    @PostMapping(path = "/sign-in")
    public void signIn(@RequestBody ResourceSignInRequestDto requestDto) {
        toolSetFacade.signIn(requestDto);
    }

    @PostMapping(path = "/sign-out")
    public void signOut(@RequestBody ResourceSignOutRequestDto requestDto) {
        toolSetFacade.signOut(requestDto);
    }
}