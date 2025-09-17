package com.epam.aidial.cfg.dto.route;

import com.epam.aidial.cfg.dto.AttachmentPathDto;
import com.epam.aidial.cfg.dto.validation.annotation.ValidDependentRoute;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@ValidDependentRoute
public class DependentRouteDto extends BaseRouteDto {

    private Set<ResourceAccessType> permissions;
    private AttachmentPathDto attachmentPaths;

    public enum ResourceAccessType {
        READ,
        WRITE,
    }
}
