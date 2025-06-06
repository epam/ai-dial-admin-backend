package com.epam.aidial.cfg.configuration;

import com.epam.aidial.cfg.dto.TokenizerDto;
import com.epam.aidial.cfg.service.TokenizerService;
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
@EnableConfigurationProperties(TokenizersConfigProperties.class)
public class TokenizersConfig {

    @Value("${config.env.tokenizers.json:}")
    private String tokenizersJson;

    @Autowired
    private TokenizersConfigProperties tokenizersConfigProperties;

    @Autowired
    private LocalValidatorFactoryBean validator;


    @SneakyThrows
    @Bean
    public TokenizerService tokenizerService() {
        List<TokenizerDto> tokenizers = new ArrayList<>();

        if (StringUtils.isNotBlank(tokenizersJson)) {
            List<TokenizerDto> tokenizerFromJson = new ObjectMapper().readValue(tokenizersJson, new TypeReference<List<TokenizerDto>>() {
            });
            tokenizers.addAll(tokenizerFromJson);
        }

        Optional.ofNullable(tokenizersConfigProperties)
                .map(TokenizersConfigProperties::getTokenizers)
                .ifPresent(tokenizers::addAll);

        tokenizers.forEach(tokenizerDto -> {
            Set<ConstraintViolation<TokenizerDto>> violationSet = validator.validate(tokenizerDto);
            if (!violationSet.isEmpty()) {
                throw new ValidationException("Validation failed for tokenizer=" + tokenizerDto + ": " + violationSet);
            }
        });

        if (tokenizers.isEmpty()) {
            tokenizers.addAll(defaultTokenizers());
        }

        return new TokenizerService(tokenizers);
    }

    private static List<TokenizerDto> defaultTokenizers() {
        return List.of(
                new TokenizerDto("gpt-3.5-turbo-0301", "gpt-3.5-turbo-0301", null),
                new TokenizerDto("gpt-4-0314", "gpt-4-0314", null),
                new TokenizerDto("gpt-4-1106-vision-preview", "gpt-4-1106-vision-preview", null)
        );
    }
}
