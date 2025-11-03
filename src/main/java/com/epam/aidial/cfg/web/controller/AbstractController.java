package com.epam.aidial.cfg.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class AbstractController {

    protected <T> ResponseEntity<T> responseEntityForGet(T obj, String newHash, String previousHash) {
        return newHash.equals(StringUtils.unwrap(previousHash, '"'))
                ? ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(newHash).build()
                : ResponseEntity.status(HttpStatus.OK).eTag(newHash).body(obj);
    }
}
