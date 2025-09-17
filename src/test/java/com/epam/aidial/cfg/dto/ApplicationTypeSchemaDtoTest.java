package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolationException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

class ApplicationTypeSchemaDtoTest {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapperConfiguration.createJsonMapper();

    @Test
    void test() throws IOException {
        // given
        File dtoJson = new File("src/test/resources/application_type_schema_dto.json");
        String expectedPropertyValue = "{\"title\":\"Temperature\",\"type\":\"number\",\"dial:meta\":{\"dial:propertyKind\":\"server\",\"dial:propertyOrder\":1}}";
        String expectedDefValue = "{\"properties\":{\"name\":{\"title\":\"Name\",\"type\":\"string\"},\"method_url\":{\"title\":\"Method Url\",\"type\":\"string\"},"
                + "\"method_type\":{\"$ref\":\"#/$defs/ToolEndpointInfoMethodType\"},\"description\":{\"title\":\"Description\",\"type\":\"string\"},"
                + "\"parameters\":{\"items\":{\"$ref\":\"#/$defs/ToolEndpointParameterInfo\"},\"title\":\"Parameters\",\"type\":\"array\"}},"
                + "\"required\":[\"name\",\"method_url\",\"method_type\",\"description\",\"parameters\"],\"title\":\"ToolEndpointInfo\",\"type\":\"object\"}";

        // when
        ApplicationTypeSchemaDto applicationTypeSchemaDto = OBJECT_MAPPER.readValue(dtoJson, ApplicationTypeSchemaDto.class);

        // then
        Assertions.assertThat(applicationTypeSchemaDto).isNotNull();
        Assertions.assertThat(applicationTypeSchemaDto.getId()).isEqualTo("https://test-schema.example");
        Assertions.assertThat(applicationTypeSchemaDto.getDescription()).isEqualTo("testDescription");
        Assertions.assertThat(applicationTypeSchemaDto.getProperties()).containsEntry("temperature", expectedPropertyValue);
        Assertions.assertThat(applicationTypeSchemaDto.getDefs()).containsEntry("ToolEndpointInfo", expectedDefValue);
        Assertions.assertThat(applicationTypeSchemaDto.getRequired()).hasSize(4).containsExactly("temperature", "instructions", "model", "web_api_toolset");
    }

    @Test
    void shouldThrowValidationExceptionIfCompletionEndpointIfInvalid() {
        // given
        File dtoJson = new File("src/test/resources/application_type_schema_dto_invalid.json");

        // when & then
        Assertions.assertThatThrownBy(() -> OBJECT_MAPPER.readValue(dtoJson, ApplicationTypeSchemaDto.class))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("ApplicationTypeSchema validation failed: "
                        + "$.dial:applicationTypeCompletionEndpoint: does not match the uri pattern must be a valid RFC 3986 URI");
    }
}