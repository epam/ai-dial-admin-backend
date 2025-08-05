package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.util.List;

@Data
@Embeddable
public class AttachmentPathEntity {
    private List<String> requestBody = List.of();
    private List<String> responseBody = List.of();
}
