package com.epam.aidial.cfg.functional.tests.history;

import com.epam.aidial.cfg.dto.ConfigRevisionDto;
import com.epam.aidial.cfg.dto.page.PageRequestDto;
import com.epam.aidial.cfg.dto.page.SortDirectionDto;
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

    public List<ConfigRevisionDto> getRevisionsList() {
        PageRequestDto pageRequestDto = new PageRequestDto();
        return historyFacade.getRevisionsList(pageRequestDto);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void rollbackToRevision(Number revision) {
        historyFacade.rollbackToRevision(revision);
    }
}
