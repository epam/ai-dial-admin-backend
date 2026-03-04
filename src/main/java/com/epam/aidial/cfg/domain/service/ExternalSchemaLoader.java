package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.domain.model.ExternalSchema;
import com.epam.aidial.cfg.exception.ApplicationTypeSchemaProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalSchemaLoader {
    private final RestTemplate restTemplate;

    public ExternalSchema fetchExternalSchema(String url) {
        try {
            return restTemplate.getForObject(url, ExternalSchema.class);

        } catch (RestClientException ex) {
            throw new ApplicationTypeSchemaProcessingException(
                    "Failed to download external schema from " + url);
        } catch (Exception ex) {
            throw new ApplicationTypeSchemaProcessingException(
                    "Failed to deserialize external schema into ExternalSchema class");
        }
    }
}