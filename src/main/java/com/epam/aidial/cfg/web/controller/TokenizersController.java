package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.TokenizerDto;
import com.epam.aidial.cfg.service.TokenizerService;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api/v1/tokenizers")
@Validated
@LogExecution
public class TokenizersController {

    private final TokenizerService tokenizerService;

    public TokenizersController(TokenizerService tokenizerService) {
        this.tokenizerService = tokenizerService;
    }

    @GetMapping(produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<TokenizerDto> getAll() {
        return tokenizerService.getAllTokenizers();
    }

}
