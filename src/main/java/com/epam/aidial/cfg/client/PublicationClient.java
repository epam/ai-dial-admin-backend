package com.epam.aidial.cfg.client;

import com.epam.aidial.cfg.client.dto.CreatePublicationDto;
import com.epam.aidial.cfg.client.dto.PublicationDto;
import com.epam.aidial.cfg.client.dto.PublicationInfosDto;
import com.epam.aidial.cfg.client.dto.PublicationPathDto;
import com.epam.aidial.cfg.client.dto.PublicationsPathDto;
import com.epam.aidial.cfg.client.dto.RejectPublicationsDto;
import com.epam.aidial.cfg.client.dto.RuleRequest;
import com.epam.aidial.cfg.client.dto.RulesDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "publicationClient", url = "${core.client.url}", configuration = AuthorizationCoreClientConfiguration.class)
public interface PublicationClient {

    @PostMapping("/v1/ops/publication/list")
    PublicationInfosDto getPublications(
            @RequestBody PublicationsPathDto publicationsPathDto
    );

    @PostMapping("/v1/ops/publication/get")
    PublicationDto getPublication(
            @RequestBody PublicationPathDto publicationPathDto
    );

    @PostMapping("/v1/ops/publication/approve")
    PublicationDto approvePublication(
            @RequestBody PublicationPathDto publicationPathDto
    );

    @PostMapping("/v1/ops/publication/reject")
    PublicationDto rejectPublication(
            @RequestBody RejectPublicationsDto rejectPublicationsDto
    );

    @PostMapping("/v1/ops/publication/create")
    PublicationDto createPublication(@RequestBody CreatePublicationDto createPublicationsDto);

    @PostMapping("/v1/ops/publication/delete")
    void deletePublication(@RequestBody PublicationPathDto publicationPathDto);

    @PostMapping("/v1/ops/publication/rule/list")
    RulesDto getRules(@RequestBody RuleRequest requestDto);

}
