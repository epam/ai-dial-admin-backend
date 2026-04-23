package com.epam.aidial.cfg.domain.util;

import com.epam.aidial.cfg.dao.model.AdapterContainerEntity;
import com.epam.aidial.cfg.dao.model.ApplicationContainerEntity;
import com.epam.aidial.cfg.dao.model.InterceptorContainerEntity;
import com.epam.aidial.cfg.dao.model.ModelContainerEntity;
import com.epam.aidial.cfg.dao.model.ToolSetContainerEntity;
import com.epam.aidial.cfg.domain.model.source.AdapterContainerSource;
import com.epam.aidial.cfg.domain.model.source.ApplicationContainerSource;
import com.epam.aidial.cfg.domain.model.source.InterceptorContainerSource;
import com.epam.aidial.cfg.domain.model.source.ModelContainerSource;
import com.epam.aidial.cfg.domain.model.source.ToolSetContainerSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContainerSourceChangeDetectorTest {

    private static final String CONTAINER_ID = "container-1";
    private static final String COMPLETION_PATH = "/api/completion";
    private static final String CONFIG_PATH = "/api/config";
    private static final String MCP_PATH = "/api/mcp";

    // --- Adapter ---

    @Test
    void adapter_noChange_returnsFalse() {
        var incoming = new AdapterContainerSource(CONTAINER_ID, "name", COMPLETION_PATH);
        var existing = adapterContainerEntity(CONTAINER_ID, COMPLETION_PATH);

        assertThat(ContainerSourceChangeDetector.hasSourceChanged(incoming, existing)).isFalse();
    }

    @Test
    void adapter_containerIdChanged_returnsTrue() {
        var incoming = new AdapterContainerSource("container-2", "name", COMPLETION_PATH);
        var existing = adapterContainerEntity(CONTAINER_ID, COMPLETION_PATH);

        assertThat(ContainerSourceChangeDetector.hasSourceChanged(incoming, existing)).isTrue();
    }

    @Test
    void adapter_completionPathChanged_returnsTrue() {
        var incoming = new AdapterContainerSource(CONTAINER_ID, "name", "/api/v2/completion");
        var existing = adapterContainerEntity(CONTAINER_ID, COMPLETION_PATH);

        assertThat(ContainerSourceChangeDetector.hasSourceChanged(incoming, existing)).isTrue();
    }

    @Test
    void adapter_containerNameDiffers_returnsFalse() {
        var incoming = new AdapterContainerSource(CONTAINER_ID, "new-name", COMPLETION_PATH);
        var existing = adapterContainerEntity(CONTAINER_ID, COMPLETION_PATH);
        existing.setContainerName("old-name");

        assertThat(ContainerSourceChangeDetector.hasSourceChanged(incoming, existing)).isFalse();
    }

    // --- Application ---

    @Test
    void application_noChange_returnsFalse() {
        var incoming = new ApplicationContainerSource(CONTAINER_ID, "name", COMPLETION_PATH, MCP_PATH);
        var existing = applicationContainerEntity(CONTAINER_ID, COMPLETION_PATH, MCP_PATH);

        assertThat(ContainerSourceChangeDetector.hasSourceChanged(incoming, existing)).isFalse();
    }

    @Test
    void application_mcpPathChanged_returnsTrue() {
        var incoming = new ApplicationContainerSource(CONTAINER_ID, "name", COMPLETION_PATH, "/api/mcp-v2");
        var existing = applicationContainerEntity(CONTAINER_ID, COMPLETION_PATH, MCP_PATH);

        assertThat(ContainerSourceChangeDetector.hasSourceChanged(incoming, existing)).isTrue();
    }

    @Test
    void application_containerIdChanged_returnsTrue() {
        var incoming = new ApplicationContainerSource("container-2", "name", COMPLETION_PATH, MCP_PATH);
        var existing = applicationContainerEntity(CONTAINER_ID, COMPLETION_PATH, MCP_PATH);

        assertThat(ContainerSourceChangeDetector.hasSourceChanged(incoming, existing)).isTrue();
    }

    // --- Model ---

    @Test
    void model_noChange_returnsFalse() {
        var incoming = new ModelContainerSource(CONTAINER_ID, "name", COMPLETION_PATH);
        var existing = modelContainerEntity(CONTAINER_ID, COMPLETION_PATH);

        assertThat(ContainerSourceChangeDetector.hasSourceChanged(incoming, existing)).isFalse();
    }

    @Test
    void model_completionPathChanged_returnsTrue() {
        var incoming = new ModelContainerSource(CONTAINER_ID, "name", "/api/v2/completion");
        var existing = modelContainerEntity(CONTAINER_ID, COMPLETION_PATH);

        assertThat(ContainerSourceChangeDetector.hasSourceChanged(incoming, existing)).isTrue();
    }

    // --- Interceptor ---

    @Test
    void interceptor_noChange_returnsFalse() {
        var incoming = new InterceptorContainerSource(CONTAINER_ID, "name", COMPLETION_PATH, CONFIG_PATH);
        var existing = interceptorContainerEntity(CONTAINER_ID, COMPLETION_PATH, CONFIG_PATH);

        assertThat(ContainerSourceChangeDetector.hasSourceChanged(incoming, existing)).isFalse();
    }

    @Test
    void interceptor_configPathChanged_returnsTrue() {
        var incoming = new InterceptorContainerSource(CONTAINER_ID, "name", COMPLETION_PATH, "/api/config-v2");
        var existing = interceptorContainerEntity(CONTAINER_ID, COMPLETION_PATH, CONFIG_PATH);

        assertThat(ContainerSourceChangeDetector.hasSourceChanged(incoming, existing)).isTrue();
    }

    @Test
    void interceptor_containerIdChanged_returnsTrue() {
        var incoming = new InterceptorContainerSource("container-2", "name", COMPLETION_PATH, CONFIG_PATH);
        var existing = interceptorContainerEntity(CONTAINER_ID, COMPLETION_PATH, CONFIG_PATH);

        assertThat(ContainerSourceChangeDetector.hasSourceChanged(incoming, existing)).isTrue();
    }

    // --- ToolSet ---

    @Test
    void toolSet_noChange_returnsFalse() {
        var incoming = new ToolSetContainerSource(CONTAINER_ID, "name", COMPLETION_PATH);
        var existing = toolSetContainerEntity(CONTAINER_ID, COMPLETION_PATH);

        assertThat(ContainerSourceChangeDetector.hasSourceChanged(incoming, existing)).isFalse();
    }

    @Test
    void toolSet_containerIdChanged_returnsTrue() {
        var incoming = new ToolSetContainerSource("container-2", "name", COMPLETION_PATH);
        var existing = toolSetContainerEntity(CONTAINER_ID, COMPLETION_PATH);

        assertThat(ContainerSourceChangeDetector.hasSourceChanged(incoming, existing)).isTrue();
    }

    // --- Null handling ---

    @Test
    void nullFieldsMatch_returnsFalse() {
        var incoming = new AdapterContainerSource(null, null, null);
        var existing = adapterContainerEntity(null, null);

        assertThat(ContainerSourceChangeDetector.hasSourceChanged(incoming, existing)).isFalse();
    }

    @Test
    void nullVsNonNull_returnsTrue() {
        var incoming = new AdapterContainerSource(CONTAINER_ID, "name", null);
        var existing = adapterContainerEntity(CONTAINER_ID, COMPLETION_PATH);

        assertThat(ContainerSourceChangeDetector.hasSourceChanged(incoming, existing)).isTrue();
    }

    // --- Helper methods ---

    private static AdapterContainerEntity adapterContainerEntity(String containerId, String completionPath) {
        var entity = new AdapterContainerEntity();
        entity.setContainerId(containerId);
        entity.setCompletionEndpointPath(completionPath);
        return entity;
    }

    private static ApplicationContainerEntity applicationContainerEntity(
            String containerId, String completionPath, String mcpPath) {
        var entity = new ApplicationContainerEntity();
        entity.setContainerId(containerId);
        entity.setCompletionEndpointPath(completionPath);
        entity.setMcpEndpointPath(mcpPath);
        return entity;
    }

    private static ModelContainerEntity modelContainerEntity(String containerId, String completionPath) {
        var entity = new ModelContainerEntity();
        entity.setContainerId(containerId);
        entity.setCompletionEndpointPath(completionPath);
        return entity;
    }

    private static InterceptorContainerEntity interceptorContainerEntity(
            String containerId, String completionPath, String configPath) {
        var entity = new InterceptorContainerEntity();
        entity.setContainerId(containerId);
        entity.setCompletionEndpointPath(completionPath);
        entity.setConfigurationEndpointPath(configPath);
        return entity;
    }

    private static ToolSetContainerEntity toolSetContainerEntity(String containerId, String completionPath) {
        var entity = new ToolSetContainerEntity();
        entity.setContainerId(containerId);
        entity.setCompletionEndpointPath(completionPath);
        return entity;
    }
}
