package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.domain.model.ExternalSchema;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExternalSchemaLoaderTest {
    private static final ObjectMapper OBJECT_MAPPER = JsonMapperConfiguration.createJsonMapper();
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ExternalSchemaLoader externalSchemaLoader;

    private static final String SCHEMA_URL = "http://mydial.epam.com/schema";
    private final String appSchema = """
            {
              "properties" : {
                  "clientFile" : {
                       "type" : "string",
                       "format" : "dial-file-encoded",
                       "dial:meta" : {
                           "dial:propertyKind" : "client",
                           "dial:propertyOrder" : 1
                       },
                       "dial:file" : true
                  },
                  "serverFile" : {
                        "type" : "string",
                        "format" : "dial-file-encoded",
                        "dial:meta" : {
                            "dial:propertyKind" : "server",
                            "dial:propertyOrder" : 2
                        },
                        "dial:file" : true
                  }
               }
            }
               """;

    @Test
    void fetchExternalSchema_ShouldReturnSchema() throws JsonProcessingException {
        var expected = OBJECT_MAPPER.readValue(appSchema, ExternalSchema.class);
        when(restTemplate.getForObject(SCHEMA_URL, ExternalSchema.class))
                .thenReturn(expected);
        var resultSchema = externalSchemaLoader.fetchExternalSchema(SCHEMA_URL);

        Assertions.assertEquals(expected, resultSchema);
    }

    @Test
    void fetchExternalSchema_whenRestCallFails_shouldThrowRuntimeException() {
        doThrow(new RestClientException("error"))
                .when(restTemplate)
                .getForObject(SCHEMA_URL, ExternalSchema.class);

        RuntimeException ex = Assertions.assertThrows(
                RuntimeException.class,
                () -> externalSchemaLoader.fetchExternalSchema(SCHEMA_URL)
        );
        Assertions.assertTrue(ex.getMessage().contains("Failed to download external schema from"));
    }
}