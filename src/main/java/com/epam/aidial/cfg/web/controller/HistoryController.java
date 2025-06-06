package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.ConfigRevisionDto;
import com.epam.aidial.cfg.dto.page.PageRequestDto;
import com.epam.aidial.cfg.dto.page.SortDirectionDto;
import com.epam.aidial.cfg.dto.revision.BaseGetRevisionQuery;
import com.epam.aidial.cfg.dto.revision.GetRevisionByIdQuery;
import com.epam.aidial.cfg.dto.revision.GetRevisionByTimestampQuery;
import com.epam.aidial.cfg.web.facade.HistoryFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Validated
@LogExecution
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryFacade historyFacade;

    @GetMapping("/history/revisions")
    public List<ConfigRevisionDto> getRevisionsList(@RequestBody @Valid PageRequestDto pageRequestDto) {
        return historyFacade.getRevisionsList(pageRequestDto);
    }

    @PostMapping("/history/revisions/query")
    public ConfigRevisionDto getRevision(@RequestBody @Valid BaseGetRevisionQuery query) {
        if (query instanceof GetRevisionByTimestampQuery revisionByTimestampQuery) {
            return historyFacade.getRevisionAtTimestamp(revisionByTimestampQuery.getTimestamp());
        } else if (query instanceof GetRevisionByIdQuery revisionByIdQuery) {
            return historyFacade.getRevisionById(revisionByIdQuery.getId());
        }
        throw new IllegalArgumentException("Unknown query type " + query);
    }
}
