package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.client.DeploymentClient;
import com.epam.aidial.cfg.client.dto.ApplicationDataDto;
import com.epam.aidial.cfg.client.dto.DeploymentDataDto;
import com.epam.aidial.cfg.client.dto.ModelCapabilitiesDataDto;
import com.epam.aidial.cfg.client.dto.ModelDataDto;
import com.epam.aidial.cfg.client.dto.ModelLimitsDataDto;
import com.epam.aidial.cfg.client.dto.ModelPricingDataDto;
import com.epam.aidial.cfg.client.dto.ToolSetDataDto;
import com.epam.aidial.cfg.client.mapper.DeploymentClientMapper;
import com.epam.aidial.cfg.model.ApplicationData;
import com.epam.aidial.cfg.model.DeploymentType;
import com.epam.aidial.cfg.model.InterfaceType;
import com.epam.aidial.cfg.model.ModelData;
import com.epam.aidial.cfg.model.ToolSetData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoreDeploymentServiceTest {

    @Mock
    private DeploymentClient deploymentClient;
    @Mock
    private DeploymentClientMapper deploymentClientMapper;

    @InjectMocks
    private CoreDeploymentService coreDeploymentService;

    @Test
    void listDeploymentsShouldRequestAllWhenInterfaceTypesNull() {
        var clientDto = DeploymentDataDto.builder().id("gpt-4").object("model").build();
        var model = ModelData.builder().id("gpt-4").object("model").build();
        when(deploymentClient.listDeployments(null)).thenReturn(List.of(clientDto));
        when(deploymentClientMapper.toDeploymentDataList(List.of(clientDto))).thenReturn(List.of(model));

        var result = coreDeploymentService.listDeployments(null, null);

        assertThat(result).containsExactly(model);
        verify(deploymentClient).listDeployments(null);
    }

    @Test
    void listDeploymentsShouldReturnEmptyListWhenCoreReturnsNull() {
        when(deploymentClient.listDeployments(null)).thenReturn(null);
        when(deploymentClientMapper.toDeploymentDataList(null)).thenReturn(List.of());

        var result = coreDeploymentService.listDeployments(null, null);

        assertThat(result).isEmpty();
    }

    @Test
    void listDeploymentsShouldPassMultipleInterfaceTypes() {
        var clientDto = ModelDataDto.builder().id("gpt-4").object("dial-model").build();
        var model = ModelData.builder().id("gpt-4").object("dial-model").build();
        when(deploymentClient.listDeployments(List.of("chat", "mcp"))).thenReturn(List.of(clientDto));
        when(deploymentClientMapper.toDeploymentDataList(List.of(clientDto))).thenReturn(List.of(model));

        var result = coreDeploymentService.listDeployments(
                List.of(InterfaceType.CHAT, InterfaceType.MCP), null);

        assertThat(result).containsExactly(model);
        verify(deploymentClient).listDeployments(List.of("chat", "mcp"));
    }

    @Test
    void listDeploymentsShouldRequestAllWhenAllTypeSpecified() {
        when(deploymentClient.listDeployments(null)).thenReturn(List.of());
        when(deploymentClientMapper.toDeploymentDataList(List.of())).thenReturn(List.of());

        coreDeploymentService.listDeployments(List.of(InterfaceType.ALL), null);

        verify(deploymentClient).listDeployments(null);
    }

    @Test
    void listDeploymentsShouldFilterByDeploymentType() {
        var model = ModelData.builder().id("m1").object("dial-model").build();
        var application = ApplicationData.builder().id("a1").object("dial-application").build();
        var clientDtos = List.of(
                ModelDataDto.builder().id("m1").object("dial-model").build(),
                ApplicationDataDto.builder().id("a1").object("dial-application").build());
        when(deploymentClient.listDeployments(null)).thenReturn(clientDtos);
        when(deploymentClientMapper.toDeploymentDataList(clientDtos)).thenReturn(List.of(model, application));

        var result = coreDeploymentService.listDeployments(null, List.of(DeploymentType.MODEL));

        assertThat(result).containsExactly(model);
    }

    @Test
    void listDeploymentsShouldMapMixedDeploymentTypesFromCore() {
        var modelDto = ModelDataDto.builder()
                .id("gpt-4")
                .object("dial-model")
                .displayName("GPT-4")
                .capabilities(ModelCapabilitiesDataDto.builder().chatCompletion(true).build())
                .limits(ModelLimitsDataDto.builder().maxPromptTokens(128000).build())
                .pricing(ModelPricingDataDto.builder().unit("token").prompt("0.1").build())
                .build();
        var applicationDto = ApplicationDataDto.builder()
                .id("my-app")
                .object("dial-application")
                .displayName("My App")
                .applicationTypeSchemaId("schema-1")
                .applicationProperties(Map.of("key", "value"))
                .build();
        var toolsetDto = ToolSetDataDto.builder()
                .id("my-toolset")
                .object("dial-toolset")
                .displayName("My Toolset")
                .transport("streamable-http")
                .allowedTools(List.of("tool-a"))
                .build();
        var clientDtos = List.of(modelDto, applicationDto, toolsetDto);
        var models = List.of(
                ModelData.builder().id("gpt-4").object("dial-model").build(),
                ApplicationData.builder().id("my-app").object("dial-application").build(),
                ToolSetData.builder().id("my-toolset").object("dial-toolset").build());

        when(deploymentClient.listDeployments(null)).thenReturn(clientDtos);
        when(deploymentClientMapper.toDeploymentDataList(clientDtos)).thenReturn(models);

        var result = coreDeploymentService.listDeployments(null, null);

        assertThat(result).hasSize(3);
        assertThat(result.get(0)).isInstanceOf(ModelData.class);
        assertThat(result.get(1)).isInstanceOf(ApplicationData.class);
        assertThat(result.get(2)).isInstanceOf(ToolSetData.class);
    }
}