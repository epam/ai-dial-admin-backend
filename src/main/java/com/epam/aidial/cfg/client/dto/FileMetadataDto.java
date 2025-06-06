package com.epam.aidial.cfg.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FileMetadataDto extends BaseMetadataDto {

    private long contentLength;
    private String contentType;
    private List<FileMetadataDto> items;

}
