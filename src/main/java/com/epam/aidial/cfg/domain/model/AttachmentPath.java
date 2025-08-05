package com.epam.aidial.cfg.domain.model;

import lombok.Data;

import java.util.List;

@Data
public class AttachmentPath {
    private List<String> requestBody = List.of();
    private List<String> responseBody = List.of();
}
