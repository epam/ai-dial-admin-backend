package com.epam.aidial.cfg.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ToolSetResourcePublicationDto extends PublicationDto {

    private List<ToolSetPublicationResourceDto> toolSetResources;
    private List<String> files;

}