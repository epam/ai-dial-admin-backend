package com.epam.aidial.cfg.client.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MoveResourceDto {

    private String sourceUrl;
    private String destinationUrl;
    private boolean overwrite;

}
