package com.epam.aidial.cfg.web.facade;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ConfigRevision;
import com.epam.aidial.cfg.domain.model.page.PageRequestModel;
import com.epam.aidial.cfg.domain.service.HistoryService;
import com.epam.aidial.cfg.dto.ConfigRevisionDto;
import com.epam.aidial.cfg.dto.page.PageRequestDto;
import com.epam.aidial.cfg.web.facade.mapper.ConfigRevisionDtoMapper;
import com.epam.aidial.cfg.web.facade.mapper.PageDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@LogExecution
public class HistoryFacade {
    private final HistoryService historyService;
    private final ConfigRevisionDtoMapper configRevisionDtoMapper;
    private final PageDtoMapper pageDtoMapper;

    public List<ConfigRevisionDto> getRevisionsList(PageRequestDto pageRequestDto) {
        PageRequestModel pageRequest = pageDtoMapper.toPageRequestModel(pageRequestDto);
        return historyService.getRevisionsList(pageRequest)
                .stream()
                .map(configRevisionDtoMapper::map)
                .collect(Collectors.toList());
    }

    public void rollbackToRevision(Number revision) {
        historyService.rollbackToRevision(revision);
    }

    public ConfigRevisionDto getRevisionById(Integer id) {
        ConfigRevision configRevision = historyService.getRevision(id);
        return configRevisionDtoMapper.map(configRevision);
    }

    public ConfigRevisionDto getRevisionAtTimestamp(Long timestamp) {
        ConfigRevision revision = historyService.getRevision(timestamp);
        return configRevisionDtoMapper.map(revision);
    }
}
