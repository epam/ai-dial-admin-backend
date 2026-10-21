package com.epam.aidial.cfg.client.dto;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class DeploymentInfoDtoTest {

    private final ObjectMapper mapper = JsonMapper.builder().build();

    @ParameterizedTest
    @CsvSource({
            "mcp, com.epam.aidial.cfg.client.dto.McpDeploymentInfoDto",
            "adapter, com.epam.aidial.cfg.client.dto.AdapterDeploymentInfoDto",
            "application, com.epam.aidial.cfg.client.dto.ApplicationDeploymentInfoDto",
            "interceptor, com.epam.aidial.cfg.client.dto.InterceptorDeploymentInfoDto",
            "nim, com.epam.aidial.cfg.client.dto.NimDeploymentInfoDto",
            "inference, com.epam.aidial.cfg.client.dto.InferenceDeploymentInfoDto"
    })
    void deserialize_deploymentManagerInternalResponse(String type, Class<?> expectedClass) {
        String json = """
                {"$type":"%s","id":"my-deployment","displayName":"My Deployment","url":"https://example.com/my-deployment"}
                """.formatted(type);

        DeploymentInfoDto dto = mapper.readValue(json, DeploymentInfoDto.class);

        assertThat(dto).isInstanceOf(expectedClass);
        assertThat(dto.getId()).isEqualTo("my-deployment");
        assertThat(dto.getDisplayName()).isEqualTo("My Deployment");
        assertThat(dto.getUrl()).isEqualTo("https://example.com/my-deployment");
    }
}
