package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.FolderInfoDto;
import com.epam.aidial.cfg.web.security.FullAdminOnly;
import com.epam.aidial.cfg.dto.MoveFolderRequestDto;
import com.epam.aidial.cfg.dto.ResourceMetadataRequestDto;
import com.epam.aidial.cfg.dto.RuleDto;
import com.epam.aidial.cfg.dto.UpdateRulesRequestDto;
import com.epam.aidial.cfg.dto.validation.annotation.MetadataPath;
import com.epam.aidial.cfg.mapper.ResourceMapper;
import com.epam.aidial.cfg.model.FolderInfo;
import com.epam.aidial.cfg.model.MoveFolderRequest;
import com.epam.aidial.cfg.model.ResourceMetadataRequest;
import com.epam.aidial.cfg.model.Rule;
import com.epam.aidial.cfg.model.UpdateRulesRequest;
import com.epam.aidial.cfg.service.FolderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/folders")
@Validated
@LogExecution
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;
    private final ResourceMapper mapper;

    @PostMapping
    public FolderInfoDto getFolders(@RequestBody @Valid ResourceMetadataRequestDto requestDto) {
        ResourceMetadataRequest request = mapper.toRequest(requestDto);
        FolderInfo folderInfo = folderService.getFolders(request);
        return mapper.toFolderInfoDto(folderInfo);
    }

    @GetMapping
    public Map<String, List<RuleDto>> getRule(@RequestParam("path") @MetadataPath String path) {
        Map<String, List<Rule>> rules = folderService.getRules(path);
        return mapper.toRuleDtos(rules);
    }

    @FullAdminOnly
    @PostMapping("/updateRules")
    public void updatesRules(@RequestBody @Valid UpdateRulesRequestDto updateRulesRequestDto) {
        UpdateRulesRequest updateRulesRequest = mapper.toUpdateRulesRequest(updateRulesRequestDto);
        folderService.updatesRules(updateRulesRequest);
    }

    @FullAdminOnly
    @DeleteMapping
    public void deleteFolder(@RequestParam("path") @MetadataPath String path) {
        folderService.unpublishFolder(path);
    }

    @FullAdminOnly
    @PostMapping("/move")
    public void moveFolder(@RequestBody @Valid MoveFolderRequestDto moveFolderRequestDto) {
        MoveFolderRequest moveFolderRequest = mapper.toMoveFolderRequest(moveFolderRequestDto);
        folderService.moveFolder(moveFolderRequest);
    }
}
