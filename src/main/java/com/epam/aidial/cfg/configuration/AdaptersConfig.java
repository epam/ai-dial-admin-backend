package com.epam.aidial.cfg.configuration;

import com.epam.aidial.cfg.dto.AdapterDto;
import com.epam.aidial.cfg.service.AdapterService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Configuration
@EnableConfigurationProperties(AdaptersConfigProperties.class)
public class AdaptersConfig {

    @Value("${config.env.adapters.json:}")
    private String adaptersJson;

    @Autowired
    private AdaptersConfigProperties adaptersConfigProperties;
    @Autowired
    private LocalValidatorFactoryBean validator;

    @SneakyThrows
    @Bean
    public AdapterService adapterService() {
        List<AdapterDto> adapters = new ArrayList<>();

        if (StringUtils.isNotBlank(adaptersJson)) {
            List<AdapterDto> adapterFromJson = new ObjectMapper().readValue(adaptersJson, new TypeReference<List<AdapterDto>>() {
            });
            adapters.addAll(adapterFromJson);
        }

        Optional.ofNullable(adaptersConfigProperties)
                .map(AdaptersConfigProperties::getAdapters)
                .ifPresent(adapters::addAll);

        adapters.forEach(adapterDto -> {
            Set<ConstraintViolation<AdapterDto>> violationSet = validator.validate(adapterDto);
            if (!violationSet.isEmpty()) {
                throw new ValidationException("Validation failed for adapter=" + adapterDto + ": " + violationSet);
            }
        });

        return new AdapterService(adapters);
    }
}
