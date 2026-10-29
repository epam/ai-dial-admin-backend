package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.dto.databind.UpstreamAuthConfigDtoDeserializer;
import com.epam.aidial.cfg.dto.databind.UpstreamAuthConfigDtoSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonDeserialize(using = UpstreamAuthConfigDtoDeserializer.class)
@JsonSerialize(using = UpstreamAuthConfigDtoSerializer.class)
public class UpstreamAuthConfigDto {

    private UpstreamAuthTypeDto type;

    // only for CUSTOM_HEADER
    private String headerName;

    public static UpstreamAuthConfigDto of(UpstreamAuthTypeDto type) {
        return new UpstreamAuthConfigDto(type, null);
    }

    public static UpstreamAuthConfigDto customHeader(String headerName) {
        return new UpstreamAuthConfigDto(UpstreamAuthTypeDto.CUSTOM_HEADER, headerName);
    }
}
