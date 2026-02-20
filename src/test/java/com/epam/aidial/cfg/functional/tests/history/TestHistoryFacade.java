package com.epam.aidial.cfg.functional.tests.history;

import com.epam.aidial.cfg.dto.AuditActivityDto;
import com.epam.aidial.cfg.dto.ConfigRevisionDto;
import com.epam.aidial.cfg.dto.PageDto;
import com.epam.aidial.cfg.dto.page.PageRequestDto;
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

    public List<ConfigRevisionDto> getRevisionsList() {
        PageRequestDto pageRequestDto = new PageRequestDto();
        return historyFacade.getRevisionsList(pageRequestDto);
    }

    public int getRevisionsListSize() {
        PageRequestDto pageRequestDto = new PageRequestDto();
        return historyFacade.getRevisionsList(pageRequestDto).size();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void rollbackToRevision(Number revision) {
        historyFacade.rollbackToRevision(revision);
    }
}
