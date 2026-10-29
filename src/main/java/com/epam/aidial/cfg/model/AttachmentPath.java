package com.epam.aidial.cfg.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class AttachmentPath {
    @Builder.Default
    private List<String> requestBody = new ArrayList<>();
    @Builder.Default
    private List<String> responseBody = new ArrayList<>();
}
