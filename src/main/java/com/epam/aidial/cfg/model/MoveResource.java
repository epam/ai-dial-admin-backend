package com.epam.aidial.cfg.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MoveResource {

    private String sourceUrl;
    private String destinationUrl;
    private boolean overwrite;

}
