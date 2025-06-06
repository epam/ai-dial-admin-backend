package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.dto.TokenizerDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class TokenizerServiceTest {

    @Test
    void getAllTokenizers() {
        List<TokenizerDto> tokenizers = List.of(new TokenizerDto());
        TokenizerService tokenizerService = new TokenizerService(tokenizers);
        List<TokenizerDto> actual = tokenizerService.getAllTokenizers();
        Assertions.assertEquals(tokenizers, actual);
    }
}