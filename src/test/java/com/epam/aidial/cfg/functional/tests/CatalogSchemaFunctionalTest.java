package com.epam.aidial.cfg.functional.tests;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.CatalogSchemaDto;
import com.epam.aidial.cfg.dto.EntitySyncStateDto;
import com.epam.aidial.cfg.dto.EntitySyncStateStatusDto;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.facade.CatalogSchemaFacade;
import com.epam.aidial.core.config.CoreCatalogSchema;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class CatalogSchemaFunctionalTest {

    @Autowired
    private CatalogSchemaFacade catalogSchemaFacade;

    private final ObjectMapper objectMapper = JsonMapperConfiguration.createJsonMapper();
    private CatalogSchemaDto dto;
    private CatalogSchemaDto dto2;

    @BeforeEach
    public void beforeEach() throws JsonProcessingException {
        var dtosJson = ResourceUtils.readResource("/catalog_schema_dto.json");
        dto = objectMapper.readValue(dtosJson, new TypeReference<>() {
        });
        dto2 = objectMapper.readValue(dtosJson, new TypeReference<>() {
        });
        dto2.setId(dto.getId() + "2");
    }

    @Test
    public void shouldSuccessfullyCreateAndGetCatalogSchema() {
        // when
        catalogSchemaFacade.create(dto);
        // then
        CatalogSchemaDto actual = catalogSchemaFacade.get(dto.getId());
        dto.setApplications(List.of());
        dto.setModels(List.of());
        dto.setToolSets(List.of());
        Assertions.assertThat(actual).isEqualTo(dto);
    }

    @Test
    public void shouldSuccessfullyUpdateCatalogSchema() {
        // given
        catalogSchemaFacade.create(dto);

        // when
        dto.setDescription("Updated description");
        dto.setRequired(List.of("category"));
        catalogSchemaFacade.update(dto.getId(), dto, "*");

        // then
        CatalogSchemaDto actual = catalogSchemaFacade.get(dto.getId());
        Assertions.assertThat(actual.getDescription()).isEqualTo("Updated description");
        Assertions.assertThat(actual.getRequired()).containsExactly("category");
    }

    @Test
    public void shouldThrowWhenCreatingDuplicateCatalogSchema() {
        // given
        catalogSchemaFacade.create(dto);

        // when & then
        Assertions.assertThatThrownBy(() -> catalogSchemaFacade.create(dto))
                .isInstanceOf(EntityAlreadyExistsException.class)
                .hasMessageContaining(dto.getId());
    }

    @Test
    public void shouldThrowWhenGettingNonExistentCatalogSchema() {
        // when & then
        Assertions.assertThatThrownBy(() -> catalogSchemaFacade.get("non-existent"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("non-existent");
    }

    @Test
    public void shouldThrowWhenUpdatingWithWrongHash() {
        // given
        catalogSchemaFacade.create(dto);

        // when & then
        dto.setDescription("Updated description");
        Assertions.assertThatThrownBy(() -> catalogSchemaFacade.update(dto.getId(), dto, "wrong-hash"))
                .isInstanceOf(OptimisticLockConflictException.class);
    }

    @Test
    public void shouldSuccessfullyGetSyncState() {
        // given
        catalogSchemaFacade.create(dto);
        var coreWithHash = catalogSchemaFacade.getCoreSchemaWithHash(dto.getId());

        // when
        EntitySyncStateDto syncState = catalogSchemaFacade.getSyncState(dto.getId(), coreWithHash.hash());

        // then - status should be one of the valid values, not null
        assertThat(syncState.getStatus()).isIn(
                EntitySyncStateStatusDto.FULLY_SYNCED,
                EntitySyncStateStatusDto.IN_PROGRESS,
                EntitySyncStateStatusDto.IN_PROGRESS_TOO_LONG,
                EntitySyncStateStatusDto.UNKNOWN
        );
    }

    @Test
    public void shouldDetectChangesWhenCoreChanged() {
        // given
        catalogSchemaFacade.create(dto);
        var coreWithHash = catalogSchemaFacade.getCoreSchemaWithHash(dto.getId());

        // when - update via Core
        CoreCatalogSchema modifiedCore = coreWithHash.core();
        modifiedCore.setDescription("Modified externally");
        String newHash = catalogSchemaFacade.updateCore(dto.getId(), modifiedCore, coreWithHash.hash());

        // then - the hash should have changed
        assertThat(newHash).isNotEqualTo(coreWithHash.hash());
    }

    @Test
    public void shouldSuccessfullyGetCoreSchema() {
        // given
        catalogSchemaFacade.create(dto);

        // when
        var coreWithHash = catalogSchemaFacade.getCoreSchemaWithHash(dto.getId());

        // then
        assertThat(coreWithHash.core()).isNotNull();
        assertThat(coreWithHash.core().getId()).isEqualTo(dto.getId());
        assertThat(coreWithHash.hash()).isNotBlank();
    }

    @Test
    public void shouldSuccessfullyUpdateCoreSchema() {
        // given
        catalogSchemaFacade.create(dto);
        var coreWithHash = catalogSchemaFacade.getCoreSchemaWithHash(dto.getId());
        CoreCatalogSchema coreSchema = coreWithHash.core();

        // when
        coreSchema.setDescription("Updated via Core");
        String newHash = catalogSchemaFacade.updateCore(dto.getId(), coreSchema, coreWithHash.hash());

        // then
        assertThat(newHash).isNotEqualTo(coreWithHash.hash());
        CatalogSchemaDto updated = catalogSchemaFacade.get(dto.getId());
        assertThat(updated.getDescription()).isEqualTo("Updated via Core");
    }

    @Test
    public void shouldListAllCatalogSchemas() {
        // given
        catalogSchemaFacade.create(dto);
        catalogSchemaFacade.create(dto2);

        // when
        var schemas = catalogSchemaFacade.getAll();

        // then
        assertThat(schemas).hasSize(2);
        assertThat(schemas).extracting(CatalogSchemaDto::getId)
                .containsExactlyInAnyOrder(dto.getId(), dto2.getId());
    }
}
