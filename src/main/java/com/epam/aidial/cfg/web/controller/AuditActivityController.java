package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.AuditActivityDto;
import com.epam.aidial.cfg.dto.PageDto;
import com.epam.aidial.cfg.dto.page.PageRequestDto;
import com.epam.aidial.cfg.web.facade.AuditActivityFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Validated
@LogExecution
@RequiredArgsConstructor
public class AuditActivityController {

    private final AuditActivityFacade auditActivityFacade;

    @PostMapping("/activities")
    public PageDto<AuditActivityDto> getAuditActivities(@RequestBody @Valid PageRequestDto pageRequestDto) {
        return auditActivityFacade.getAuditActivities(pageRequestDto);
    }

    @GetMapping("/activities/{activityId}")
    public AuditActivityDto getAuditActivity(@PathVariable UUID activityId) {
        return auditActivityFacade.getAuditActivity(activityId);
    }
}