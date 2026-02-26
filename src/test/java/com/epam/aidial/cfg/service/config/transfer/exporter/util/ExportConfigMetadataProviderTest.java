package com.epam.aidial.cfg.service.config.transfer.exporter.util;

import com.epam.aidial.cfg.domain.model.ExportConfigComponentMetadata;
import com.epam.aidial.cfg.domain.model.ExportConfigMetadata;
import com.epam.aidial.cfg.domain.model.ExportFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.epam.aidial.cfg.domain.model.ExportConfigComponentType.ADAPTER;
import static com.epam.aidial.cfg.domain.model.ExportConfigComponentType.APPLICATION;
import static com.epam.aidial.cfg.domain.model.ExportConfigComponentType.APPLICATION_TYPE_SCHEMA;
import static com.epam.aidial.cfg.domain.model.ExportConfigComponentType.GLOBAL_INTERCEPTOR;
import static com.epam.aidial.cfg.domain.model.ExportConfigComponentType.INTERCEPTOR;
import static com.epam.aidial.cfg.domain.model.ExportConfigComponentType.INTERCEPTOR_RUNNER;
import static com.epam.aidial.cfg.domain.model.ExportConfigComponentType.KEY;
import static com.epam.aidial.cfg.domain.model.ExportConfigComponentType.MODEL;
import static com.epam.aidial.cfg.domain.model.ExportConfigComponentType.ROLE;
import static com.epam.aidial.cfg.domain.model.ExportConfigComponentType.ROUTE;
import static com.epam.aidial.cfg.domain.model.ExportConfigComponentType.TOOL_SET;
import static org.assertj.core.api.Assertions.assertThat;

class ExportConfigMetadataProviderTest {

    private ExportConfigMetadataProvider exportConfigMetadataProvider;

    @BeforeEach
    void setUp() {
        exportConfigMetadataProvider = new ExportConfigMetadataProvider();
    }

    @ParameterizedTest
    @MethodSource("getMetadata_shouldReturnCorrectExportConfigComponentsMetadata_testParams")
    void getMetadata_shouldReturnCorrectExportConfigComponentsMetadata(ExportFormat exportFormat,
                                                                       List<ExportConfigComponentMetadata> components) {
        // given
        ExportConfigMetadata expected = ExportConfigMetadata.builder()
                .components(components)
                .build();

        // when
        ExportConfigMetadata actual = exportConfigMetadataProvider.getMetadata(exportFormat);

        // then
        assertThat(actual.getComponents()).usingRecursiveComparison().isEqualTo(expected.getComponents());
    }

    private static Stream<Arguments> getMetadata_shouldReturnCorrectExportConfigComponentsMetadata_testParams() {
        return Stream.of(
                // admin format
                Arguments.of(
                        ExportFormat.ADMIN,
                        List.of(ExportConfigComponentMetadata.builder()
                                        .type(MODEL)
                                        .dependencies(Set.of(ADAPTER, INTERCEPTOR, INTERCEPTOR_RUNNER))
                                        .build(),
                                ExportConfigComponentMetadata.builder()
                                        .type(APPLICATION)
                                        .dependencies(Set.of(INTERCEPTOR, APPLICATION_TYPE_SCHEMA, INTERCEPTOR_RUNNER))
                                        .build(),
                                ExportConfigComponentMetadata.builder()
                                        .type(ROUTE)
                                        .dependencies(Set.of())
                                        .build(),
                                ExportConfigComponentMetadata.builder()
                                        .type(TOOL_SET)
                                        .dependencies(Set.of())
                                        .build(),
                                ExportConfigComponentMetadata.builder()
                                        .type(ROLE)
                                        .dependencies(Set.of(MODEL, APPLICATION, TOOL_SET, ROUTE, ADAPTER, INTERCEPTOR, INTERCEPTOR_RUNNER, APPLICATION_TYPE_SCHEMA))
                                        .build(),
                                ExportConfigComponentMetadata.builder()
                                        .type(KEY)
                                        .dependencies(Set.of(ROLE, MODEL, APPLICATION, TOOL_SET, ROUTE, ADAPTER, INTERCEPTOR, INTERCEPTOR_RUNNER, APPLICATION_TYPE_SCHEMA))
                                        .build(),
                                ExportConfigComponentMetadata.builder()
                                        .type(INTERCEPTOR)
                                        .dependencies(Set.of(INTERCEPTOR_RUNNER))
                                        .build(),
                                ExportConfigComponentMetadata.builder()
                                        .type(INTERCEPTOR_RUNNER)
                                        .dependencies(Set.of())
                                        .build(),
                                ExportConfigComponentMetadata.builder()
                                        .type(APPLICATION_TYPE_SCHEMA)
                                        .dependencies(Set.of(INTERCEPTOR, INTERCEPTOR_RUNNER))
                                        .build(),
                                ExportConfigComponentMetadata.builder()
                                        .type(ADAPTER)
                                        .dependencies(Set.of())
                                        .build(),
                                ExportConfigComponentMetadata.builder()
                                        .type(GLOBAL_INTERCEPTOR)
                                        .dependencies(Set.of(INTERCEPTOR, INTERCEPTOR_RUNNER))
                                        .build())
                ),

                // core format
                Arguments.of(
                        ExportFormat.CORE,
                        List.of(ExportConfigComponentMetadata.builder()
                                        .type(MODEL)
                                        .dependencies(Set.of(INTERCEPTOR))
                                        .build(),
                                ExportConfigComponentMetadata.builder()
                                        .type(APPLICATION)
                                        .dependencies(Set.of(INTERCEPTOR, APPLICATION_TYPE_SCHEMA))
                                        .build(),
                                ExportConfigComponentMetadata.builder()
                                        .type(ROUTE)
                                        .dependencies(Set.of())
                                        .build(),
                                ExportConfigComponentMetadata.builder()
                                        .type(TOOL_SET)
                                        .dependencies(Set.of())
                                        .build(),
                                ExportConfigComponentMetadata.builder()
                                        .type(ROLE)
                                        .dependencies(Set.of(MODEL, APPLICATION, TOOL_SET, ROUTE, INTERCEPTOR, APPLICATION_TYPE_SCHEMA))
                                        .build(),
                                ExportConfigComponentMetadata.builder()
                                        .type(KEY)
                                        .dependencies(Set.of(ROLE, MODEL, APPLICATION, TOOL_SET, ROUTE, INTERCEPTOR, APPLICATION_TYPE_SCHEMA))
                                        .build(),
                                ExportConfigComponentMetadata.builder()
                                        .type(INTERCEPTOR)
                                        .dependencies(Set.of())
                                        .build(),
                                ExportConfigComponentMetadata.builder()
                                        .type(APPLICATION_TYPE_SCHEMA)
                                        .dependencies(Set.of(INTERCEPTOR))
                                        .build(),
                                ExportConfigComponentMetadata.builder()
                                        .type(GLOBAL_INTERCEPTOR)
                                        .dependencies(Set.of(INTERCEPTOR))
                                        .build())
                )
        );
    }
}