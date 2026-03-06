package com.epam.aidial.cfg.functional.tests.history;

import com.epam.aidial.cfg.domain.model.page.SortDirection;
import com.epam.aidial.cfg.dto.AuditActivityDto;
import com.epam.aidial.cfg.dto.ConfigRevisionDto;
import com.epam.aidial.cfg.dto.PageDto;
import com.epam.aidial.cfg.dto.page.PageRequestDto;
import com.epam.aidial.cfg.dto.page.SortDto;
import com.epam.aidial.cfg.dto.page.filter.FilterDto;
import com.epam.aidial.cfg.dto.page.filter.FilterOperatorDto;
import com.epam.aidial.cfg.web.facade.AuditActivityFacade;
import com.epam.aidial.cfg.web.facade.HistoryFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TestHistoryFacade {

    private final HistoryFacade historyFacade;
    private final AuditActivityFacade activityFacade;

    public PageDto<AuditActivityDto> getActivities() {
        PageRequestDto pageRequestDto = new PageRequestDto();
        return activityFacade.getAuditActivities(pageRequestDto);
    }

    public PageDto<AuditActivityDto> getActivitiesAtRevision(int revision) {
        FilterDto revisionEqFilter = new FilterDto("revision", FilterOperatorDto.eq, String.valueOf(revision));

        SortDto resourceTypeSort = new SortDto("resourceType", SortDirection.ASC);
        SortDto resourceIdSort = new SortDto("resourceId", SortDirection.ASC);

        PageRequestDto pageRequestDto = new PageRequestDto();
        pageRequestDto.setFilters(List.of(revisionEqFilter));
        pageRequestDto.setSorts(List.of(resourceTypeSort, resourceIdSort));

        return activityFacade.getAuditActivities(pageRequestDto);
    }

    public List<ConfigRevisionDto> getRevisionsList() {
        PageRequestDto pageRequestDto = new PageRequestDto();
        return historyFacade.getRevisionsList(pageRequestDto);
    }

    public int getRevisionsListSize() {
        return getRevisionsList().size();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void rollbackToRevision(Number revision) {
        historyFacade.rollbackToRevision(revision);
    }
}
