package com.epam.aidial.cfg.dto;

import lombok.Data;

import java.util.List;

@Data
public class AttachmentPathDto {
    private List<String> requestBody = List.of();
    private List<String> responseBody = List.of();
}
