package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.service.DescriptionKeywordsService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api/v1/topics")
@Validated
@RequiredArgsConstructor
@LogExecution
public class TopicController {

    private final DescriptionKeywordsService descriptionKeywordsService;

    @GetMapping(produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<String> getAllDescriptionKeywords() {
        return descriptionKeywordsService.getAllDescriptionKeywords();
    }

}
