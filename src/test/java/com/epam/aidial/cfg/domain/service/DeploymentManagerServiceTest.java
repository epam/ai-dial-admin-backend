package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.client.deployment.manager.DeploymentManagerClient;
import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.client.dto.InferenceDeploymentInfoDto;
import com.epam.aidial.cfg.exception.DeploymentClientNotExistsException;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeploymentManagerServiceTest {

    private static final String ID = "container-id";
    private static final long CACHE_EXPIRATION_MS = 300_000L;
    private static final String CLIENT_URL = "http://deployment-manager";

    @Mock
    private DeploymentManagerClient deploymentManagerClient;

    private DeploymentManagerService service;

    @BeforeEach
    void setUp() {
        service = new DeploymentManagerService(deploymentManagerClient, CACHE_EXPIRATION_MS, CLIENT_URL);
    }

    private DeploymentInfoDto deploymentWithUrl(String url) {
        InferenceDeploymentInfoDto dto = new InferenceDeploymentInfoDto();
        dto.setId(ID);
        dto.setDisplayName("display");
        dto.setUrl(url);
        return dto;
    }

    @Test
    void getById_cachesDeploymentWithUrl() {
        DeploymentInfoDto deployed = deploymentWithUrl("http://container-url/v1");
        when(deploymentManagerClient.getDeployment(ID)).thenReturn(deployed);

        DeploymentInfoDto first = service.getById(ID);
        DeploymentInfoDto second = service.getById(ID);

        assertThat(first).isSameAs(deployed);
        assertThat(second).isSameAs(deployed);
        // Second call must be served from cache — client hit exactly once.
        verify(deploymentManagerClient, times(1)).getDeployment(ID);
    }

    @Test
    void getById_doesNotCacheDeploymentWithBlankUrl() {
        DeploymentInfoDto notReady = deploymentWithUrl(null);
        when(deploymentManagerClient.getDeployment(ID)).thenReturn(notReady);

        DeploymentInfoDto first = service.getById(ID);
        DeploymentInfoDto second = service.getById(ID);

        assertThat(first).isSameAs(notReady);
        assertThat(second).isSameAs(notReady);
        // Blank-URL result must NOT be cached, so both calls refetch — this is the #3813 fix.
        verify(deploymentManagerClient, times(2)).getDeployment(ID);
    }

    @Test
    void getById_refetchesAfterUrlBecomesAvailable() {
        DeploymentInfoDto notReady = deploymentWithUrl("   ");
        DeploymentInfoDto ready = deploymentWithUrl("http://container-url/v1");
        when(deploymentManagerClient.getDeployment(ID)).thenReturn(notReady, ready);

        DeploymentInfoDto blank = service.getById(ID);
        DeploymentInfoDto populated = service.getById(ID);

        assertThat(blank).isSameAs(notReady);
        // Once the URL is populated the fresh result is returned (not a stale cached blank).
        assertThat(populated).isSameAs(ready);
        verify(deploymentManagerClient, times(2)).getDeployment(ID);
    }

    @Test
    void getById_throwsWhenDeploymentClientNotConfigured() {
        DeploymentManagerService placeholderService =
                new DeploymentManagerService(deploymentManagerClient, CACHE_EXPIRATION_MS, "url-placeholder");

        assertThatThrownBy(() -> placeholderService.getById(ID))
                .isInstanceOf(DeploymentClientNotExistsException.class);
    }

    @Test
    void getById_returnsNullWhenDeploymentNotFound() {
        when(deploymentManagerClient.getDeployment(ID)).thenThrow(notFound());

        assertThat(service.getById(ID)).isNull();
    }

    private FeignException.NotFound notFound() {
        Request request = Request.create(
                Request.HttpMethod.GET, "/", Collections.emptyMap(), null, StandardCharsets.UTF_8, new RequestTemplate());
        return new FeignException.NotFound("not found", request, null, Collections.emptyMap());
    }
}