package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Embeddable
public class AttachmentPathEntity {
    @Column(name = "request_body_paths")
    private List<String> requestBody = new ArrayList<>();
    @Column(name = "response_body_paths")
    private List<String> responseBody = new ArrayList<>();
}
