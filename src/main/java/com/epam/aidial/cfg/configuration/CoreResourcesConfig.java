package com.epam.aidial.cfg.configuration;

import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.service.ResourceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration(proxyBeanMethods = false)
public class CoreResourcesConfig {

    @Bean
    public Map<ResourceType, ResourceService> resourceServicesByResourceType(List<ResourceService> resourceServices) {
        return resourceServices.stream()
                .collect(Collectors.toMap(ResourceService::getResourceType, Function.identity()));
    }
}
