package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ExternalSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@LogExecution
@Slf4j
@RequiredArgsConstructor
public class ExternalSchemaLoader {
    private final RestTemplate restTemplate;

    public ExternalSchema fetchExternalSchema(String url) {
        try {
            return restTemplate.getForObject(url, ExternalSchema.class);

        } catch (RestClientException ex) {
            log.warn("Failed to download external schema from " + url);
            throw new RuntimeException(
                    "Failed to download external schema from " + url, ex);
        } catch (Exception ex) {
            log.warn("Failed to deserialize external schema into ExternalSchema class");
            throw new RuntimeException(
                    "Failed to deserialize external schema into ExternalSchema class", ex);
        }
    }
}