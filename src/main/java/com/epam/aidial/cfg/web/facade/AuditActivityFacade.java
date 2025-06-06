package com.epam.aidial.cfg.web.facade;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.AuditActivity;
import com.epam.aidial.cfg.domain.model.Page;
import com.epam.aidial.cfg.domain.model.page.PageRequestModel;
import com.epam.aidial.cfg.domain.service.AuditActivityService;
import com.epam.aidial.cfg.dto.AuditActivityDto;
import com.epam.aidial.cfg.dto.PageDto;
import com.epam.aidial.cfg.dto.page.PageRequestDto;
import com.epam.aidial.cfg.web.facade.mapper.AuditActivityDtoMapper;
import com.epam.aidial.cfg.web.facade.mapper.PageDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@LogExecution
public class AuditActivityFacade {
    private final AuditActivityService auditActivityService;
    private final AuditActivityDtoMapper auditActivityDtoMapper;
    private final PageDtoMapper pageDtoMapper;

    public PageDto<AuditActivityDto> getAuditActivities(PageRequestDto pageRequestDto) {
        PageRequestModel pageRequest = pageDtoMapper.toPageRequestModel(pageRequestDto);
        Page<AuditActivity> page = auditActivityService.getActivitiesList(pageRequest);

        List<AuditActivityDto> data = page
                .getData()
                .stream()
                .map(auditActivityDtoMapper::map)
                .collect(Collectors.toList());

        return PageDto.<AuditActivityDto>builder()
                .data(data)
                .total(page.getTotal())
                .totalPages(page.getTotalPages())
                .build();
    }

    public AuditActivityDto getAuditActivity(UUID activityId) {
        AuditActivity activity = auditActivityService.getActivity(activityId);
        return auditActivityDtoMapper.map(activity);
    }
}
