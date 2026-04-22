package com.epam.aidial.cfg.client.mapper;

import com.epam.aidial.cfg.client.dto.PublicationDto;
import com.epam.aidial.cfg.client.dto.PublicationStatusDto;
import com.epam.aidial.cfg.client.dto.ResourceTypeDto;
import com.epam.aidial.cfg.model.CreatePublication;
import com.epam.aidial.cfg.model.PromptPublication;
import com.epam.aidial.cfg.model.PublicationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PublicationClientMapperImplTest {

    private PublicationClientMapperImpl mapper;

    @BeforeEach
    void setUp() {
        mapper = new PublicationClientMapperImpl();
    }

    @Test
    void toPublicationDtoShouldPercentEncodeSpacesInTargetFolder() {
        var publication = PromptPublication.builder()
                .path("publication1")
                .folderId("public/target folder/")
                .requestName("publication name")
                .status(PublicationStatus.PENDING)
                .resources(Collections.emptyList())
                .build();

        var dto = mapper.toPublicationDto(publication, Collections.emptyList());

        assertThat(dto.getTargetFolder()).isEqualTo("public/target%20folder/");
        assertThat(dto.getUrl()).isEqualTo("publications/publication1");
    }

    @Test
    void toCreatePublicationDtoShouldPercentEncodeSpacesInTargetFolder() {
        var createPublication = CreatePublication.builder()
                .targetFolder("public/new folder/")
                .resources(Collections.emptyList())
                .build();

        var dto = mapper.toCreatePublicationDto(createPublication);

        assertThat(dto.getTargetFolder()).isEqualTo("public/new%20folder/");
    }

    @Test
    void getPublicationMappingShouldDecodeTargetFolderToFolderId() {
        var clientDto = new PublicationDto();
        clientDto.setUrl("publications/publication1");
        clientDto.setName("publication name");
        clientDto.setTargetFolder("public/target%20folder/");
        clientDto.setStatus(PublicationStatusDto.PENDING);
        clientDto.setResourceTypes(List.of(ResourceTypeDto.PROMPT));
        clientDto.setResources(Collections.emptyList());

        var model = mapper.toPromptPublication(clientDto, "publication1", Collections.emptyList(), Collections.emptyList());

        assertThat(model.getFolderId()).isEqualTo("public/target folder/");
    }
}