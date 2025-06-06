package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.dto.TokenizerDto;
import org.springframework.util.Assert;

import java.util.List;

public class TokenizerService {

    private final List<TokenizerDto> tokenizers;

    public TokenizerService(List<TokenizerDto> tokenizers) {
        Assert.notNull(tokenizers, "tokenizers must be not null");
        this.tokenizers = tokenizers;
    }

    public List<TokenizerDto> getAllTokenizers() {
        return tokenizers;
    }
}
