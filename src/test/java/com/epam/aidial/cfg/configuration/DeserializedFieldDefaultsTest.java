package com.epam.aidial.cfg.configuration;

import com.epam.aidial.cfg.client.dto.CreatePublicationDto;
import com.epam.aidial.cfg.client.dto.McpResourceDto;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.dto.ExportConfigComponentDto;
import com.epam.aidial.cfg.dto.page.PageRequestDto;
import com.epam.aidial.cfg.model.AttachmentPath;
import com.epam.aidial.cfg.model.CreatePublication;
import com.epam.aidial.core.config.CoreAuthenticationType;
import com.epam.aidial.core.config.CoreResourceAuthSettings;
import com.epam.aidial.core.config.CoreShareResourceLimit;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Jackson 3 builds a class through its all-args constructor when parameter names are available, so a property
 * absent from the JSON gets the type default (0, false, null) instead of the field initializer.
 * Each test deserializes an (almost) empty JSON and expects the initializers to survive.
 */
class DeserializedFieldDefaultsTest {

    private final ObjectMapper mapper = JsonMapperConfiguration.createJsonMapper();
    private final ObjectMapper coreMapper = JsonMapperConfiguration.createCoreJsonMapper();

    @Test
    void coreShareResourceLimit_absentProperties_keepUnlimitedDefaults() {
        for (ObjectMapper objectMapper : new ObjectMapper[]{mapper, coreMapper}) {
            CoreShareResourceLimit limit = objectMapper.readValue("{}", CoreShareResourceLimit.class);

            assertThat(limit.getMaxAcceptedUsers()).isEqualTo(-1);
            assertThat(limit.getInvitationTtl()).isEqualTo(-1L);
        }
    }

    @Test
    void coreResourceAuthSettings_absentAuthenticationType_defaultsToNone() {
        String json = "{\"token_endpoint_auth_method\":\"client_secret_basic\"}";

        for (ObjectMapper objectMapper : new ObjectMapper[]{mapper, coreMapper}) {
            CoreResourceAuthSettings settings = objectMapper.readValue(json, CoreResourceAuthSettings.class);

            assertThat(settings.getAuthenticationType()).isEqualTo(CoreAuthenticationType.NONE);
        }
    }

    @Test
    void mcpResourceDto_absentProperties_keepDefaults() {
        McpResourceDto mcp = mapper.readValue("{\"endpoint\":\"http://mcp.test\"}", McpResourceDto.class);

        assertThat(mcp.getTransport()).isEqualTo(McpResourceDto.TransportDto.HTTP);
        assertThat(mcp.getConfigDelivery()).isEqualTo(McpResourceDto.McpConfigDeliveryDto.META);
        assertThat(mcp.isForwardPerRequestKey()).isTrue();
    }

    @Test
    void mcpResourceDto_explicitNullForwardPerRequestKey_keepsDefault() {
        McpResourceDto mcp = mapper.readValue("{\"forwardPerRequestKey\":null}", McpResourceDto.class);

        assertThat(mcp.isForwardPerRequestKey()).isTrue();
    }

    @Test
    void mcpResourceDto_explicitFalse_isKept() {
        McpResourceDto mcp = mapper.readValue("{\"forwardPerRequestKey\":false}", McpResourceDto.class);

        assertThat(mcp.isForwardPerRequestKey()).isFalse();
    }

    @Test
    void roleLimit_absentEnabled_defaultsToTrue() {
        RoleLimit roleLimit = mapper.readValue("{\"role\":\"admin\"}", RoleLimit.class);

        assertThat(roleLimit.isEnabled()).isTrue();
    }

    @Test
    void roleLimit_explicitNullEnabled_keepsDefault() {
        RoleLimit roleLimit = mapper.readValue("{\"role\":\"admin\",\"enabled\":null}", RoleLimit.class);

        assertThat(roleLimit.isEnabled()).isTrue();
    }

    @Test
    void roleLimit_explicitFalse_isKept() {
        RoleLimit roleLimit = mapper.readValue("{\"role\":\"admin\",\"enabled\":false}", RoleLimit.class);

        assertThat(roleLimit.isEnabled()).isFalse();
    }

    @Test
    void pageRequestDto_absentProperties_keepDefaults() {
        PageRequestDto page = mapper.readValue("{}", PageRequestDto.class);

        assertThat(page.getPageNumber()).isZero();
        assertThat(page.getPageSize()).isEqualTo(20);
        assertThat(page.getSorts()).isEmpty();
        assertThat(page.getFilters()).isEmpty();
    }

    @Test
    void pageRequestDto_explicitNullPageSize_keepsDefault() {
        PageRequestDto page = mapper.readValue("{\"pageSize\":null}", PageRequestDto.class);

        assertThat(page.getPageSize()).isEqualTo(20);
    }

    @Test
    void pageRequestDto_explicitPageSize_isKept() {
        PageRequestDto page = mapper.readValue("{\"pageSize\":50}", PageRequestDto.class);

        assertThat(page.getPageSize()).isEqualTo(50);
    }

    @Test
    void attachmentPath_absentProperties_defaultToEmptyLists() {
        AttachmentPath path = mapper.readValue("{}", AttachmentPath.class);

        assertThat(path.getRequestBody()).isEmpty();
        assertThat(path.getResponseBody()).isEmpty();
    }

    @Test
    void createPublication_absentResources_defaultsToEmptyList() {
        CreatePublication publication = mapper.readValue("{\"targetFolder\":\"folder\"}", CreatePublication.class);

        assertThat(publication.getResources()).isEmpty();
    }

    @Test
    void createPublicationDto_absentResources_defaultsToEmptyList() {
        CreatePublicationDto publication = mapper.readValue("{\"targetFolder\":\"folder\"}", CreatePublicationDto.class);

        assertThat(publication.getResources()).isEmpty();
    }

    @Test
    void exportConfigComponentDto_absentDependencies_defaultsToEmptySet() {
        ExportConfigComponentDto component = mapper.readValue("{\"name\":\"model\",\"type\":\"MODEL\"}", ExportConfigComponentDto.class);

        assertThat(component.getDependencies()).isEmpty();
    }
}
