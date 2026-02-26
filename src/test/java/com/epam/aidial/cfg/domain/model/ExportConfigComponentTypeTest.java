package com.epam.aidial.cfg.domain.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExportConfigComponentTypeTest {

    @ParameterizedTest
    @MethodSource("getAllDependencies_shouldReturnSetOfAllComponentDependencies_testParams")
    void getAllDependencies_shouldReturnSetOfAllComponentDependencies(ExportConfigComponentType type,
                                                                      ExportFormat exportFormat,
                                                                      Set<ExportConfigComponentType> expectedDependencies) {
        assertThat(type.getAllDependencies(exportFormat)).containsExactlyInAnyOrderElementsOf(expectedDependencies);
    }

    @ParameterizedTest
    @MethodSource("getAllDependencies_shouldThrowExceptionWhenUnsupportedExportFormat_testParams")
    void getAllDependencies_shouldThrowExceptionWhenUnsupportedExportFormat(ExportConfigComponentType type,
                                                                            ExportFormat exportFormat) {
        assertThatThrownBy(() -> type.getAllDependencies(exportFormat))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Export format: " + exportFormat + " is not supported for: " + type);
    }

    private static Stream<Arguments> getAllDependencies_shouldReturnSetOfAllComponentDependencies_testParams() {
        return Stream.of(
                // admin format
                Arguments.of(MODEL, ExportFormat.ADMIN, Set.of(ADAPTER, INTERCEPTOR, INTERCEPTOR_RUNNER)),
                Arguments.of(APPLICATION, ExportFormat.ADMIN, Set.of(INTERCEPTOR, APPLICATION_TYPE_SCHEMA, INTERCEPTOR_RUNNER)),
                Arguments.of(ROUTE, ExportFormat.ADMIN, Set.of()),
                Arguments.of(TOOL_SET, ExportFormat.ADMIN, Set.of()),
                Arguments.of(ROLE, ExportFormat.ADMIN, Set.of(MODEL, APPLICATION, TOOL_SET, ROUTE, ADAPTER, INTERCEPTOR, INTERCEPTOR_RUNNER, APPLICATION_TYPE_SCHEMA)),
                Arguments.of(KEY, ExportFormat.ADMIN, Set.of(ROLE, MODEL, APPLICATION, TOOL_SET, ROUTE, ADAPTER, INTERCEPTOR, INTERCEPTOR_RUNNER, APPLICATION_TYPE_SCHEMA)),
                Arguments.of(INTERCEPTOR, ExportFormat.ADMIN, Set.of(INTERCEPTOR_RUNNER)),
                Arguments.of(INTERCEPTOR_RUNNER, ExportFormat.ADMIN, Set.of()),
                Arguments.of(APPLICATION_TYPE_SCHEMA, ExportFormat.ADMIN, Set.of(INTERCEPTOR, INTERCEPTOR_RUNNER)),
                Arguments.of(ADAPTER, ExportFormat.ADMIN, Set.of()),
                Arguments.of(GLOBAL_INTERCEPTOR, ExportFormat.ADMIN, Set.of(INTERCEPTOR, INTERCEPTOR_RUNNER)),

                // core format
                Arguments.of(MODEL, ExportFormat.CORE, Set.of(INTERCEPTOR)),
                Arguments.of(APPLICATION, ExportFormat.CORE, Set.of(INTERCEPTOR, APPLICATION_TYPE_SCHEMA)),
                Arguments.of(ROUTE, ExportFormat.CORE, Set.of()),
                Arguments.of(TOOL_SET, ExportFormat.CORE, Set.of()),
                Arguments.of(ROLE, ExportFormat.CORE, Set.of(MODEL, APPLICATION, TOOL_SET, ROUTE, INTERCEPTOR, APPLICATION_TYPE_SCHEMA)),
                Arguments.of(KEY, ExportFormat.CORE, Set.of(ROLE, MODEL, APPLICATION, TOOL_SET, ROUTE, INTERCEPTOR, APPLICATION_TYPE_SCHEMA)),
                Arguments.of(INTERCEPTOR, ExportFormat.CORE, Set.of()),
                Arguments.of(APPLICATION_TYPE_SCHEMA, ExportFormat.CORE, Set.of(INTERCEPTOR)),
                Arguments.of(GLOBAL_INTERCEPTOR, ExportFormat.ADMIN, Set.of(INTERCEPTOR, INTERCEPTOR_RUNNER))
        );
    }

    private static Stream<Arguments> getAllDependencies_shouldThrowExceptionWhenUnsupportedExportFormat_testParams() {
        return Stream.of(
                Arguments.of(INTERCEPTOR_RUNNER, ExportFormat.CORE),
                Arguments.of(ADAPTER, ExportFormat.CORE)
        );
    }
}