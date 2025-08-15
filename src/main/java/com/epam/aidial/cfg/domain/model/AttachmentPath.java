package com.epam.aidial.cfg.domain.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AttachmentPath {
    private List<String> requestBody = new ArrayList<>();
    private List<String> responseBody = new ArrayList<>();
}
