package com.epam.aidial.cfg.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentPath {
    @Builder.Default
    private List<String> requestBody = new ArrayList<>();
    @Builder.Default
    private List<String> responseBody = new ArrayList<>();
}
