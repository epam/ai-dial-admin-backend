package com.epam.aidial.cfg.dto.route;

import com.epam.aidial.cfg.dto.AttachmentPathDto;
import com.epam.aidial.cfg.dto.validation.annotation.DependentRoute;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.SortedSet;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@DependentRoute
public class DependentRouteDto extends BaseRouteDto {

    private SortedSet<ResourceAccessType> permissions;
    private AttachmentPathDto attachmentPaths;

    public enum ResourceAccessType {
        READ,
        WRITE,
    }
}
