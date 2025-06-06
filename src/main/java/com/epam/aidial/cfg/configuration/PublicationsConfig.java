package com.epam.aidial.cfg.configuration;

import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.service.publication.resolver.PublicationResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration(proxyBeanMethods = false)
public class PublicationsConfig {

    @Bean
    public Map<ResourceType, PublicationResolver> publicationResolversByResourceType(List<PublicationResolver> publicationResolvers) {
        return publicationResolvers.stream()
                .collect(Collectors.toMap(PublicationResolver::getResourceType, Function.identity()));
    }
}
