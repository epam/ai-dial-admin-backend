package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.PublicationDto;
import com.epam.aidial.cfg.dto.PublicationInfosDto;
import com.epam.aidial.cfg.dto.PublicationPathDto;
import com.epam.aidial.cfg.dto.RejectPublicationDto;
import com.epam.aidial.cfg.dto.ResourceTypeDto;
import com.epam.aidial.cfg.mapper.PublicationMapper;
import com.epam.aidial.cfg.service.publication.PublicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/publications")
@Validated
@LogExecution
@RequiredArgsConstructor
public class PublicationController {

    private final PublicationService publicationService;
    private final PublicationMapper publicationMapper;

    @GetMapping(produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public PublicationInfosDto getAllPublications(@RequestParam(value = "type", required = false) String type) {
        var resourceTypeDto = Optional.ofNullable(type).map(ResourceTypeDto::fromString).orElse(null);
        var resourceType = publicationMapper.toResourceType(resourceTypeDto);

        var publicationInfos = publicationService.getAllPublications(resourceType);
        return publicationMapper.toPromptPublicationInfosDto(publicationInfos);
    }

    @PostMapping(path = "/get",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE,
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public PublicationDto getPublication(@RequestBody PublicationPathDto publicationPathDto) {
        var publication = publicationService.getPublication(publicationPathDto.getPath());
        return publicationMapper.toPublicationDto(publication);
    }

    @PostMapping(path = "/approve",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE,
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public void approvePublication(@RequestBody PublicationPathDto publicationPathDto) {
        publicationService.approvePublication(publicationPathDto.getPath());
    }

    @PostMapping(path = "/reject",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE,
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public void rejectPublication(@RequestBody RejectPublicationDto rejectPublicationDto) {
        publicationService.rejectPublication(rejectPublicationDto.getPath(), rejectPublicationDto.getComment());
    }

}
