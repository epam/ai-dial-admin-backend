package com.epam.aidial.cfg.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AttachmentPathDto {
    private List<String> requestBody = new ArrayList<>();
    private List<String> responseBody = new ArrayList<>();
}
