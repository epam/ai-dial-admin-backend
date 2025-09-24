package com.epam.aidial.cfg.domain.model.source;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class InterceptorContainerSource extends InterceptorSource {
    private String containerId;
    private String containerName;
    private String completionEndpointPath;
    private String configurationEndpointPath;
}
